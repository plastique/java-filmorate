package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository {
    List<Film> getAll();

    Film findById(Long id);

    Film create(Film film);

    Film update(Film film);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> getPopular(int count);
}
