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
    FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> list() {
        return filmService.getList();
    }

    @PostMapping
    public Film create(@RequestBody final Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody final Film filmUpdate) {
        return filmService.update(filmUpdate);
    }
}
