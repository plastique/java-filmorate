package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.contracts.*;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private static final int DESCRIPTION_LIMIT = 200;
    private static final LocalDate RELEASE_MIN_DATE = LocalDate.of(1895, 12, 28);

    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    public List<Film> getList() {
        return filmRepository.getAll();
    }

    public Film getFilm(final Long id) {
        Film film = filmRepository.findById(id);

        if (film == null) {
            throw new NotFoundException("Film not found");
        }

        return film;
    }

    public Film create(final Film film) {
        validate(film);
        return filmRepository.create(film);
    }

    public Film update(final Film film) {
        validate(film);

        if (!filmRepository.isExists(film.getId())) {
            throw new NotFoundException("Film not found");
        }

        return filmRepository.update(film);
    }

    public void addLike(final Long id, final Long userId) {
        validateFilmId(id);
        validateUserId(userId);
        filmAndUserExists(id, userId);

        likeRepository.addLike(id, userId);
    }

    public void deleteLike(final Long id, final Long userId) {
        validateFilmId(id);
        validateUserId(userId);
        filmAndUserExists(id, userId);

        likeRepository.deleteLike(id, userId);
    }

    public List<Film> getPopular(final int count) {
        if (count <= 0) {
            throw new ValidationException("Count must be greater than 0");
        }

        return filmRepository.getPopular(count);
    }

    private void filmAndUserExists(final Long filmId, final Long userId) {
        if (!filmRepository.isExists(filmId)) {
            throw new NotFoundException("Film not found");
        }

        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }
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
            throw new ValidationException("Film release date is null or incorrect");
        }

        if (film.getDuration() < 0) {
            throw new ValidationException("Film duration is negative");
        }

        if (
            film.getMpa() != null
            && film.getMpa().getId() != null
            && mpaRepository.findById(film.getMpa().getId()) == null
        ) {
            throw new ValidationException("Mpa not found");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Long> availableIds = genreRepository.getAll().stream().map(Genre::getId).toList();
            film.getGenres().forEach(genre -> {
                if (genre.getId() == null || !availableIds.contains(genre.getId())) {
                    throw new ValidationException("Invalid Genre id");
                }
            });
        }
    }

    private void validateFilmId(final Long id) {
        if (id == null) {
            throw new ValidationException("Film id is null");
        }
    }

    private void validateUserId(final Long id) {
        if (id == null) {
            throw new ValidationException("User id is null");
        }
    }
}
