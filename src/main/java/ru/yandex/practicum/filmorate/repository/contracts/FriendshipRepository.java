package ru.yandex.practicum.filmorate.repository.contracts;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipRepository {
    void addFriend(final Long userId, final Long friendId);

    void deleteFriend(final Long userId, final Long friendId);

    List<User> findFriendsByUserId(final Long userId);

    List<User> findCommonFriends(Long userId, Long friendId);
}
