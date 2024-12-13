package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserRepository {
    Collection<User> getAll();

    User findById(Long id);

    User create(User user);

    User update(User user);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> findFriendsByUserId(Long userId);

    Collection<User> findCommonFriends(Long userId, Long otherUserId);

    boolean exists(Long id);
}
