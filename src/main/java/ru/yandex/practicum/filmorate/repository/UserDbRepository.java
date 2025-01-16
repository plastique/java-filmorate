package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbRepository implements UserRepository {

    public static final String TABLE_NAME = "users";

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper = new UserRowMapper();

    @Override
    public List<User> getAll() {
        return jdbc.query(
                "SELECT * FROM " + TABLE_NAME,
                mapper
        );
    }

    public User findById(final Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT * FROM " + TABLE_NAME + " WHERE id = ?",
                    mapper,
                    id
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public User create(final User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(
                    conn -> {
                        PreparedStatement stmt = conn.prepareStatement(
                                "INSERT INTO " + TABLE_NAME + " (email, login, name, birthday) " +
                                        "VALUES (?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );

                        stmt.setString(1, user.getEmail());
                        stmt.setString(2, user.getLogin());
                        stmt.setString(3, user.getName());
                        stmt.setDate(4, Date.valueOf(user.getBirthday()));

                        return stmt;
                    },
                    keyHolder
            );
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new InternalErrorException("Error on saving data");
        }

        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            throw new InternalErrorException("Error on saving data");
        }

        user.setId(id);

        return user;
    }

    @Override
    public User update(final User user) {
        try {
            jdbc.update(
                    "UPDATE " + TABLE_NAME + " " +
                            "SET email = ?, login = ?, name = ?, birthday = ? " +
                            "WHERE id = ?",
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    Date.valueOf(user.getBirthday()),
                    user.getId()
            );
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new InternalErrorException("Error on updating data");
        }

        return user;
    }

    @Override
    public boolean isExists(final Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT id FROM " + TABLE_NAME + " WHERE id = ?",
                    Long.class,
                    id
            ) != null;
        } catch (DataAccessException e) {
            return false;
        }
    }
}
