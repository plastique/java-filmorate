package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.contracts.FilmRepository;
import ru.yandex.practicum.filmorate.repository.contracts.GenreRepository;
import ru.yandex.practicum.filmorate.repository.contracts.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbRepository implements FilmRepository {
    public static final String TABLE_NAME = "films";
    private static final String FILM_GENRE_TABLE_NAME = "film_genre";

    private final JdbcTemplate jdbc;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final FilmRowMapper mapper = new FilmRowMapper();

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbc.query(
                "SELECT f.*, m.name mpa_name " +
                        "FROM " + TABLE_NAME + " AS f " +
                        "LEFT JOIN " + MpaDbRepository.TABLE_NAME + " AS m ON m.id = f.mpa_id",
                mapper
        );

        if (!films.isEmpty()) {
            fillGenreForFilmList(films);
        }

        return films;
    }

    @Override
    public Film findById(final Long id) {
        try {
            Film film = jdbc.queryForObject(
                    "SELECT f.*, m.name mpa_name " +
                            "FROM " + TABLE_NAME + " AS f " +
                            "LEFT JOIN " + MpaDbRepository.TABLE_NAME + " AS m ON m.id = f.mpa_id " +
                            "WHERE f.id = ?",
                    mapper,
                    id
            );

            fillGenreForFilm(film);

            return film;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public Film create(final Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Mpa mpa = getMpaByObject(film.getMpa());

        try {
            jdbc.update(
                    conn -> {
                        PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO " + TABLE_NAME + " (mpa_id, name, description, release_date, duration) " +
                                        "VALUES (?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );

                        stmt.setLong(1, mpa == null ? 0L : mpa.getId());
                        stmt.setString(2, film.getName());
                        stmt.setString(3, film.getDescription());
                        stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
                        stmt.setInt(5, film.getDuration());

                        return stmt;
                    },
                    keyHolder
            );
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InternalErrorException("Error on saving data");
        }

        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            throw new InternalErrorException("Error on saving data");
        }

        film.setId(id);
        addRelations(film);

        return film;
    }

    @Override
    public Film update(final Film film) {
        Mpa mpa = getMpaByObject(film.getMpa());

        try {
            jdbc.update(
                    "UPDATE " + TABLE_NAME + " " +
                            "SET mpa_id = ?, name = ?, description = ?, release_date = ?, duration = ? " +
                            "WHERE id = ?",
                    mpa == null ? 0L : mpa.getId(),
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getId()
            );
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InternalErrorException("Error on updating data");
        }

        deleteFilmGenres(film.getId());
        addRelations(film);

        return findById(film.getId());
    }

    @Override
    public List<Film> getPopular(int count) {
        return jdbc.query(
                "SELECT f.*, m.name mpa_name, COUNT(l.film_id) like_cnt " +
                        "FROM " + TABLE_NAME + " AS f " +
                        "LEFT JOIN " + LikeDbRepository.TABLE_NAME + " as l ON (l.film_id = f.id) " +
                        "LEFT JOIN " + MpaDbRepository.TABLE_NAME + " as m ON (m.id = f.mpa_id) " +
                        "GROUP BY f.id " +
                        "ORDER BY like_cnt DESC " +
                        "LIMIT ?",
                mapper,
                count
        );
    }

    @Override
    public boolean isExists(Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT id FROM " + TABLE_NAME + " WHERE id = ?",
                    Long.class,
                    id
            ) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Mpa getMpaByObject(Mpa mpa) {
        if (mpa == null || mpa.getId() == null) {
            return null;
        }

        try {
            return mpaRepository.findById(mpa.getId());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void addRelations(Film film) {
        Set<Genre> genres = film.getGenres().stream().filter(Objects::nonNull).collect(Collectors.toSet());

        if (genres.isEmpty()) {
            return;
        }

        Iterator<Genre> genreIterator = genres.iterator();

        try {
            jdbc.batchUpdate(
                    "INSERT INTO " + FILM_GENRE_TABLE_NAME + " (film_id, genre_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Genre genre = genreIterator.next();
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genre.getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    }
            );
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InternalErrorException("Error on saving film genre");
        }
    }

    private void deleteFilmGenres(Long filmId) {
        if (filmId == null) {
            return;
        }

        try {
            jdbc.update("DELETE FROM " + FILM_GENRE_TABLE_NAME + " WHERE film_id = ?", filmId);
        } catch (RuntimeException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    private void fillGenreForFilm(Film film) {
        if (film == null) {
            return;
        }

        film.getGenres().addAll(genreRepository.findByFilmId(film.getId()));
    }

    private void fillGenreForFilmList(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Map<Long, List<Genre>> genres = genreRepository.findByFilmId(
                films.stream().map(Film::getId).toList()
        );

        if (genres.isEmpty()) {
            return;
        }

        films.forEach((Film film) -> film.getGenres().addAll(
                genres.getOrDefault(film.getId(), new ArrayList<>())
        ));
    }
}
