package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Collection<User> getList() {
        return userRepository.getAll();
    }

    public User create(final User user) {
        validateUserData(user);
        setNameIfEmpty(user);

        return userRepository.create(user);
    }

    public User update(final User user) {
        validateUserData(user);
        setNameIfEmpty(user);

        return userRepository.update(user);
    }

    public Collection<User> getUserFriends(final Long userId) {
        return userRepository.findFriendsByUserId(userId);
    }

    public Collection<User> getCommonFriends(final Long id, final Long otherId) {
        return userRepository.findCommonFriends(id, otherId);
    }

    public void addFriend(final Long userId, final Long friendId) {
        validateUserFriend(userId, friendId);

        userRepository.addFriend(userId, friendId);
    }

    public void deleteFriend(final Long userId, final Long friendId) {
        validateUserFriend(userId, friendId);

        userRepository.deleteFriend(userId, friendId);
    }

    private void validateUserData(final User user) {
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

    private void validateUserFriend(final Long userId, final Long friendId) {
        if (userId == null || friendId == null) {
            throw new ValidationException("userId and friendId can't be null");
        }

        if (userId.equals(friendId)) {
            throw new ValidationException("User and friend can't be the same");
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
}
