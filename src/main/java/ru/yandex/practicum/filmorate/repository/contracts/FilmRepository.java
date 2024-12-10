package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmRepository {
    Collection<Film> getAll();
    Film findById(Long id);
    Film create(Film film);
    Film update(Film film);
}
