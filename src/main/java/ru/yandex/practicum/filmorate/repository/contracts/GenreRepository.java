package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreRepository {
    List<Genre> getAll();

    List<Genre> findByFilmId(Long filmId);

    Map<Long, List<Genre>> findByFilmId(List<Long> filmIds);

    Genre findById(Long id);
}
