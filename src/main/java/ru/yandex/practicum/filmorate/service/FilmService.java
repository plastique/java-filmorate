package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.contracts.FilmRepository;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class FilmService {
    private static final int DESCRIPTION_LIMIT = 200;
    private static final LocalDate RELEASE_MIN_DATE = LocalDate.of(1895, 12, 28);

    private final FilmRepository filmRepository;

    @Autowired
    public FilmService(final FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    public Collection<Film> getList() {
        return filmRepository.getAll();
    }

    public Film create(final Film film) {
        validate(film);
        return filmRepository.create(film);
    }

    public Film update(final Film film) {
        validate(film);
        return filmRepository.update(film);
    }

    private void validate(final Film film) {
        if (film == null) {
            throw new ValidationException("Film is null");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Film name is null or empty");
        }

        if (film.getDescription() == null || film.getDescription().length() > DESCRIPTION_LIMIT) {
            throw new ValidationException("Film description is too long");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(RELEASE_MIN_DATE)) {
            throw new ValidationException("Film description is too long");
        }

        if (film.getDuration() < 0) {
            throw new ValidationException("Film duration is negative");
        }
    }
}
