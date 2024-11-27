package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private static Long increment = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> list() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody final User user) {
        try {
            validate(user);
        } catch (final ValidationException e) {
            log.error("User create validation error: {}", e.getMessage());
            throw e;
        }

        user.setId(getNextId());
        setNameIfEmpty(user);

        log.info("Create user: {}", user);
        users.put(user.getId(), user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody final User userUpdate) {
        try {
            validate(userUpdate);
        } catch (final ValidationException e) {
            log.error("User update validation error: {}", e.getMessage());
            throw e;
        }

        if (!users.containsKey(userUpdate.getId())) {
            log.error("User update not found: {}", userUpdate.getId());
            throw new IllegalArgumentException("User update not found");
        }

        User user = users.get(userUpdate.getId());

        user.setLogin(userUpdate.getLogin());
        user.setName(userUpdate.getName());
        user.setEmail(userUpdate.getEmail());
        user.setBirthday(userUpdate.getBirthday());
        setNameIfEmpty(user);

        log.info("Update user: {}", user);
        users.put(user.getId(), user);

        return user;
    }

    private void validate(final User user) {
        if (user == null) {
            throw new ValidationException("User is null");
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Email is not valid");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("User login empty or contains spaces");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday is not valid");
        }
    }

    private void setNameIfEmpty(final User user) {
        if (user == null) {
            return;
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private Long getNextId() {
        return increment++;
    }
}
