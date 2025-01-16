package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.repository.contracts.LikeRepository;

@Repository
@RequiredArgsConstructor
public class LikeDbRepository implements LikeRepository {
    public static final String TABLE_NAME = "film_like";

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(final Long filmId, final Long userId) {
        try {
            jdbc.update(
                    "INSERT INTO " + TABLE_NAME + " (film_id, user_id) VALUES (?, ?)",
                    filmId,
                    userId
            );
        } catch (DataAccessException e) {
            throw new InternalErrorException("Error on adding like");
        }
    }

    @Override
    public void deleteLike(final Long filmId, final Long userId) {
        try {
            jdbc.update(
                    "DELETE FROM " + TABLE_NAME + " WHERE film_id = ? AND user_id = ?",
                    filmId,
                    userId
            );
        } catch (DataAccessException ignored) {
        }
    }
}
