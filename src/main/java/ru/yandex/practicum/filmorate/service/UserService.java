package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.FriendshipRepository;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public List<User> getList() {
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

        if (!userRepository.isExists(user.getId())) {
            throw new NotFoundException("User not found");
        }

        return userRepository.update(user);
    }

    public List<User> getUserFriends(final Long userId) {
        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        return friendshipRepository.findFriendsByUserId(userId);
    }

    public List<User> getCommonFriends(final Long id, final Long otherId) {
        if (!userRepository.isExists(id)) {
            throw new NotFoundException("User not found");
        }

        if (!userRepository.isExists(otherId)) {
            throw new NotFoundException("Friend not found");
        }

        return friendshipRepository.findCommonFriends(id, otherId);
    }

    public void addFriend(final Long userId, final Long friendId) {
        validateUserFriend(userId, friendId);

        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!userRepository.isExists(friendId)) {
            throw new NotFoundException("Friend not found");
        }

        friendshipRepository.addFriend(userId, friendId);
    }

    public void deleteFriend(final Long userId, final Long friendId) {
        validateUserFriend(userId, friendId);

        if (!userRepository.isExists(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!userRepository.isExists(friendId)) {
            throw new NotFoundException("Friend not found");
        }

        friendshipRepository.deleteFriend(userId, friendId);
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
