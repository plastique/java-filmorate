package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserRepository implements UserRepository {
    private static Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User findById(Long id) {
        return users.get(id);
    }

    @Override
    public User create(User user) {

        User newUser = new User(
                getNextId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );

        users.put(newUser.getId(), newUser);

        return user;
    }

    @Override
    public User update(User user) {

        if (!users.containsKey(user.getId())) {
            throw new IllegalArgumentException("User not found");
        }

        User updatedUser = users.get(user.getId());

        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setName(user.getName());
        updatedUser.setBirthday(user.getBirthday());

        users.put(updatedUser.getId(), updatedUser);

        return updatedUser;
    }

    private Long getNextId() {
        return id++;
    }
}
