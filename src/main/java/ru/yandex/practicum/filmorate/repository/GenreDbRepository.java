package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.contracts.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbRepository implements GenreRepository {
    public static final String TABLE_NAME = "genres";
    private static final String FILM_GENRE_TABLE_NAME = "film_genre";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper = new GenreRowMapper();

    @Override
    public List<Genre> getAll() {
        return jdbc.query(
                "SELECT * FROM " + TABLE_NAME,
                mapper
        );
    }

    @Override
    public List<Genre> findByFilmId(final Long filmId) {
        return jdbc.query(
                "SELECT g.* " +
                "FROM " + TABLE_NAME + " AS g " +
                "INNER JOIN " + FILM_GENRE_TABLE_NAME + " AS fg ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ?",
                mapper,
                filmId
        );
    }

    @Override
    public Map<Long, List<Genre>> findByFilmId(final List<Long> filmIds) {
        Map<Long, List<Genre>> genresMap = new HashMap<>();
        SqlRowSet rowSet = jdbc.queryForRowSet(
                String.format(
                    "SELECT g.*, fg.film_id " +
                    "FROM " + TABLE_NAME + " AS g " +
                    "INNER JOIN " + FILM_GENRE_TABLE_NAME + " AS fg ON fg.genre_id = g.id " +
                    "WHERE fg.film_id IN (%s)",
                    filmIds.stream().map(el -> Long.toString(el)).collect(Collectors.joining(","))
                )
        );
        while (rowSet.next()) {
            Genre genre = Genre.builder()
                    .id(rowSet.getLong("id"))
                    .name(rowSet.getString("name"))
                    .build();

            Long filmId = rowSet.getLong("film_id");
            List<Genre> genres = genresMap.getOrDefault(filmId, new ArrayList<>());
            genres.add(genre);

            genresMap.put(filmId, genres);
        }

        return genresMap;
    }

    @Override
    public Genre findById(final Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT * FROM " + TABLE_NAME + " WHERE id = ?",
                    mapper,
                    id
            );
        } catch (DataAccessException e) {
            return null;
        }
    }
}
