package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    User findById(Long id);

    User create(User user);

    User update(User user);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> findFriendsByUserId(Long userId);

    List<User> findCommonFriends(Long userId, Long otherUserId);

    boolean isExists(Long id);
}
