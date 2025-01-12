package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.contracts.FilmRepository;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InMemoryFilmRepository implements FilmRepository {
    private static Long id = 1L;
    private final Map<Long, Film> films = new HashMap<>();
    private final UserRepository userRepository;

    public List<Film> getAll() {
        return films.values().stream().toList();
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

        films.put(newFilm.getId(), newFilm);

        return newFilm;
    }

    @Override
    public Film update(final Film film) {
        Film updatedFilm = findById(film.getId());

        updatedFilm.setName(film.getName());
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());

        films.put(updatedFilm.getId(), updatedFilm);

        return updatedFilm;
    }

    @Override
    public void addLike(final Long id, final Long userId) {
        Film film = findById(id);

        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        film.getLikes().add(userId);
        films.put(id, film);
    }

    @Override
    public void deleteLike(final Long id, final Long userId) {
        Film film = findById(id);

        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        film.getLikes().remove(userId);
        films.put(id, film);
    }

    @Override
    public List<Film> getPopular(int count) {
        return films.values()
                .stream()
                .sorted((Film a, Film b) -> b.getLikes().size() - a.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Long getNextId() {
        return id++;
    }
}
