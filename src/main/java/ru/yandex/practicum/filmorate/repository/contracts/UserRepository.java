package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    User findById(Long id);

    User create(User user);

    User update(User user);

    boolean isExists(Long id);
}
