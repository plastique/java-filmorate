package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final int DESCRIPTION_LIMIT = 200;
    private static Long increment = 1L;
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> list() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody final Film film) {
        try {
            validate(film);
        } catch (ValidationException e) {
            log.error("Film create validation error: {}", e.getMessage());
            throw e;
        }

        film.setId(getNextId());
        log.info("Create film: {}", film);
        films.put(film.getId(), film);

        return film;
    }

    @PutMapping
    public Film update(@RequestBody final Film filmUpdate) {
        try {
            validate(filmUpdate);
        } catch (ValidationException e) {
            log.error("Film update validation error: {}", e.getMessage());
            throw e;
        }

        if (!films.containsKey(filmUpdate.getId())) {
            log.error("Film update not found: {}", filmUpdate.getId());
            throw new IllegalArgumentException("Film update not found");
        }

        Film film = films.get(filmUpdate.getId());

        film.setName(filmUpdate.getName());
        film.setDescription(filmUpdate.getDescription());
        film.setReleaseDate(filmUpdate.getReleaseDate());
        film.setDuration(filmUpdate.getDuration());

        log.info("Update film: {}", film);
        films.put(film.getId(), film);

        return film;
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

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Film description is too long");
        }

        if (film.getDuration() < 0) {
            throw new ValidationException("Film duration is negative");
        }
    }

    private Long getNextId() {
        return increment++;
    }
}
