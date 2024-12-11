package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.contracts.FilmRepository;

import java.util.*;

@Component
public class InMemoryFilmRepository implements FilmRepository {
    private static Long id = 1L;
    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getAll() {
        return films.values();
    }

    public Film findById(final Long id) {
        return Optional
                .ofNullable(films.get(id))
                .orElseThrow(() -> new NotFoundException("Film not found"));
    }

    @Override
    public Film create(final Film film) {

        Film newFilm = Film.builder()
                .id(getNextId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();

        films.put(film.getId(), newFilm);

        return newFilm;
    }

    @Override
    public Film update(final Film film) {

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film not found");
        }

        Film updatedFilm = films.get(film.getId());

        updatedFilm.setName(film.getName());
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());

        films.put(updatedFilm.getId(), updatedFilm);

        return updatedFilm;
    }

    private Long getNextId() {
        return id++;
    }
}
