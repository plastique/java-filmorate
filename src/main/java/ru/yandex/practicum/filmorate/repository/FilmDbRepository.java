package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Primary
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
                "SELECT * FROM ? ORDER BY name",
                mapper,
                TABLE_NAME
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
                    "SELECT * FROM ? WHERE id = ?",
                    mapper,
                    TABLE_NAME,
                    id
            );

            fillGenreForFilm(film);

            return film;
        } catch (RuntimeException e) {
            throw new NotFoundException("Film not found");
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
                                "INSERT INTO ? (mpa_id, name, description, release_date, duration) " +
                                        "VALUES (?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );

                        stmt.setString(1, TABLE_NAME);
                        stmt.setLong(2, mpa == null ? 0L : mpa.getId());
                        stmt.setString(3, film.getName());
                        stmt.setString(4, film.getDescription());
                        stmt.setDate(5, Date.valueOf(film.getReleaseDate()));
                        stmt.setInt(6, film.getDuration());

                        return stmt;
                    },
                    keyHolder
            );
        } catch (RuntimeException e) {
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
        int updated = 0;

        try {
            updated = jdbc.update(
                    "UPDATE ? " +
                            "SET mpa_id = ?, name = ?, description = ?, release_date = ?, duration = ? " +
                            "WHERE id = ?",
                    TABLE_NAME,
                    mpa == null ? 0L : mpa.getId(),
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getId()
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on updating data");
        }

        if (updated < 1) {
            throw new InternalErrorException("Error on updating data");
        }

        syncFilmGenres(film);

        return findById(film.getId());
    }

    @Override
    public List<Film> getPopular(int count) {
        return jdbc.query(
                "SELECT f.*, COUNT(l.id) like_cnt " +
                        "FROM ? AS f " +
                        "LEFT JOIN ? as l ON (l.film_id = f.id) " +
                        "GROUP BY f.id " +
                        "ORDER BY likes DESC " +
                        "LIMIT ?",
                mapper,
                TABLE_NAME,
                LikeDbRepository.TABLE_NAME,
                count
        );
    }

    @Override
    public boolean isExists(Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT id FROM ? WHERE id = ?",
                    mapper,
                    TABLE_NAME,
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

        try {
            jdbc.batchUpdate(
                    "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Genre genre = genres.iterator().next();
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genre.getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    }
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on saving film genre");
        }
    }

    private void syncFilmGenres(Film film) {
        if (film == null) {
            return;
        }

        List<Long> availableIds = genreRepository.getAll().stream().map(Genre::getId).toList();
        List<Long> deleteRows = new ArrayList<>();
        List<Long> newGenres = film.getGenres()
                .stream()
                .filter(
                        el -> Objects.nonNull(el)
                                && Objects.nonNull(el.getId())
                                && availableIds.contains(el.getId())
                )
                .map(Genre::getId)
                .toList();

        SqlRowSet curGenres = jdbc.queryForRowSet(
                "SELECT id, genre_id FROM ? WHERE film_id = ?",
                FILM_GENRE_TABLE_NAME,
                film.getId()
        );

        while (curGenres.next()) {
            Long id = curGenres.getLong("id");
            Long genreId = curGenres.getLong("genre_id");

            if (newGenres.contains(genreId)) {
                newGenres.remove(genreId);
                continue;
            }

            deleteRows.add(id);
        }

        if (!deleteRows.isEmpty()) {
            try {
                jdbc.update(
                        "DELETE FROM ? WHERE id IN (?)",
                        FILM_GENRE_TABLE_NAME,
                        String.join(", ", deleteRows.stream().map(String::valueOf).toList())
                );
            } catch (RuntimeException ignored) {
            }
        }

        if (newGenres.isEmpty()) {
            return;
        }

        try {
            jdbc.batchUpdate(
                    "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Long genreId = newGenres.iterator().next();
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genreId);
                        }

                        public int getBatchSize() {
                            return newGenres.size();
                        }
                    }
            );
        } catch (RuntimeException ignored) {
        }
    }

//    private void deleteFilmGenres(Long filmId) {
//        if (filmId == null) {
//            return;
//        }
//
//        try {
//            jdbc.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
//        } catch (DataAccessException e) {
//            throw new InternalErrorException(e.getMessage());
//        }
//    }

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
