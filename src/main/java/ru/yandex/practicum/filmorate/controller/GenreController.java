package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @Autowired
    public GenreController(final GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public Collection<Genre> list() {
        return genreService.getList();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable final Long id) {
        return genreService.getById(id);
    }
}
