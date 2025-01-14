package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.FriendshipRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbRepository implements FriendshipRepository {

    public static final String TABLE_NAME = "friendship";

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper = new UserRowMapper();

    @Override
    public void addFriend(final Long userId, final Long friendId) {
        try {
            jdbc.update(
                    "INSERT INTO " + TABLE_NAME + " (user_id, friend_id, active) VALUES (?, ?, ?)",
                    userId,
                    friendId,
                    false
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on adding friend");
        }
    }

    @Override
    public void deleteFriend(final Long userId, final Long friendId) {
        try {
            jdbc.update(
                    "DELETE FROM " + TABLE_NAME + " WHERE user_id = ? AND friend_id = ?",
                    userId,
                    friendId
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on deleting friend");
        }
    }

    @Override
    public List<User> findFriendsByUserId(final Long userId) {
        try {
            return jdbc.query(
                    "SELECT f.* " +
                            "FROM " + UserDbRepository.TABLE_NAME + " AS f " +
                            "WHERE f.id IN (" +
                            "SELECT fs.friend_id FROM " + TABLE_NAME + " AS fs WHERE fs.user_id = ?" +
                            ")",
                    userRowMapper,
                    userId
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on getting user friends");
        }
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long friendId) {
        try {
            return jdbc.query(
                    "SELECT f.* " +
                            "FROM " + UserDbRepository.TABLE_NAME + " AS f " +
                            "WHERE f.id IN (SELECT fs1.friend_id FROM " + TABLE_NAME + " AS fs1 WHERE fs1.user_id = ?) " +
                            "AND f.id IN (SELECT fs2.friend_id FROM " + TABLE_NAME + " AS fs2 WHERE fs2.user_id = ?)",
                    userRowMapper,
                    userId,
                    friendId
            );
        } catch (RuntimeException e) {
            throw new InternalErrorException("Error on getting user friends");
        }
    }
}
