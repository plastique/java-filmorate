package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.contracts.UserRepository;

import java.util.*;

@Component
public class InMemoryUserRepository implements UserRepository {
    private static Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    public User findById(final Long id) {
        return Optional
                .ofNullable(users.get(id))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User create(final User user) {
        User newUser = User.builder()
                .id(getNextId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);

        return newUser;
    }

    @Override
    public User update(final User user) {
        User updatedUser = findById(user.getId());

        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setName(user.getName());
        updatedUser.setBirthday(user.getBirthday());

        users.put(updatedUser.getId(), updatedUser);

        return updatedUser;
    }

    @Override
    public void addFriend(final Long userId, final Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        users.put(user.getId(), user);
        users.put(friend.getId(), friend);
    }

    @Override
    public void deleteFriend(final Long userId, final Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        users.put(user.getId(), user);
        users.put(friend.getId(), friend);
    }

    @Override
    public List<User> findFriendsByUserId(final Long userId) {
        Set<Long> friends = findById(userId).getFriends();
        List<User> friendsList = new ArrayList<>();

        if (friends.isEmpty()) {
            return friendsList;
        }

        friends.forEach((Long id) -> {
            User friend = findById(id);
            friendsList.add(friend);
        });

        return friendsList;
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherUserId) {
        Set<Long> userFriends = findById(userId).getFriends();
        Set<Long> otherUserFriends = findById(otherUserId).getFriends();

        List<User> friendsList = new ArrayList<>();

        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
            return friendsList;
        }

        userFriends.stream()
                .filter(otherUserFriends::contains)
                .forEach((Long id) -> friendsList.add(findById(id)));

        return friendsList;
    }

    @Override
    public boolean isExists(final Long id) {
        return users.containsKey(id);
    }

    private void validateUser(final Long id) {
        if (!isExists(id)) {
            throw new NotFoundException("User not found");
        }
    }

    private Long getNextId() {
        return id++;
    }
}
