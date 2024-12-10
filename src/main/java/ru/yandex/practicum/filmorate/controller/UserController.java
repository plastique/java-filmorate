package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> list() {
        return userService.getList();
    }

    @PostMapping
    public User create(@RequestBody final User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody final User user) {
        return userService.update(user);
    }
}
