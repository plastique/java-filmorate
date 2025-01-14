package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> list() {
        return filmService.getList();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable final Long id) {
        return filmService.getFilm(id);
    }

    @PostMapping
    public Film create(@RequestBody final Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody final Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable final Long id,
            @PathVariable final Long userId
    ) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable final Long id,
            @PathVariable final Long userId
    ) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> popular(
            @RequestParam(required = false, defaultValue = "10") final int count
    ) {
        return filmService.getPopular(count);
    }
}
