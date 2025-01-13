package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.contracts.FilmRepository;
import ru.yandex.practicum.filmorate.repository.contracts.LikeRepository;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

@Repository
@RequiredArgsConstructor
@Primary
public class LikeDbRepository implements LikeRepository {
    public static final String TABLE_NAME = "film_like";

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    @Override
    public void addLike(final Long filmId, final Long userId) {
        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!filmRepository.isExists(filmId)) {
            throw new NotFoundException("Film not found");
        }

        try {
            jdbc.update(
                    "INSERT INTO " + TABLE_NAME + " (film_id, user_id) VALUES (?, ?)",
                    filmId,
                    userId
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on adding like");
        }
    }

    @Override
    public void deleteLike(final Long filmId, final Long userId) {
        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!filmRepository.isExists(filmId)) {
            throw new NotFoundException("Film not found");
        }

        try {
            jdbc.update(
                    "DELETE FROM " + TABLE_NAME + " WHERE film_id = ? AND user_id = ?",
                    filmId,
                    userId
            );
        } catch (RuntimeException ignored) {
        }
    }
}
