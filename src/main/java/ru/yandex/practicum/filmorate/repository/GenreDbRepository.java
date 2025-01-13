package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.contracts.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Primary
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
                "SELECT g.*" +
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
        jdbc.queryForList(
                "SELECT g.*, fg.film_id " +
                    "FROM " + TABLE_NAME + " AS g " +
                    "INNER JOIN " + FILM_GENRE_TABLE_NAME + " AS fg ON fg.genre_id = g.id " +
                    "WHERE fg.film_id IN (?)",
                String.join(
                        ",",
                        filmIds.stream().map(String::valueOf).toList()
                )
        ).forEach(v -> {
            Genre genre = Genre.builder()
                    .id((Long)v.get("id"))
                    .name((String)v.get("name"))
                    .build();

            Long filmId = (Long)v.get("film_id");
            List<Genre> genres = genresMap.getOrDefault(filmId, new ArrayList<>());
            genres.add(genre);

            genresMap.put(filmId, genres);
        });

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
        } catch (RuntimeException e) {
            throw new NotFoundException("Genre not found");
        }
    }
}
