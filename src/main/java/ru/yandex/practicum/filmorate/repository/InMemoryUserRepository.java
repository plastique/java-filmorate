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
    public Collection<User> getAll() {
        return users.values();
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

        return user;
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
        userExistsOrException(friendId);

        User user = findById(userId);
        user.getFriends().add(friendId);

        users.put(userId, user);
    }

    @Override
    public void deleteFriend(final Long userId, final Long friendId) {
        userExistsOrException(friendId);

        User user = findById(userId);
        user.getFriends().remove(friendId);

        users.put(userId, user);
    }

    @Override
    public Collection<User> findFriendsByUserId(final Long userId) {
        Set<Long> friends = findById(userId).getFriends();
        List<User> friendsList = new ArrayList<>();

        if (friends.isEmpty()) {
            return friendsList;
        }

        friends.stream().peek((Long id) -> friendsList.add(findById(id)));

        return friendsList;
    }

    @Override
    public Collection<User> findCommonFriends(Long userId, Long otherUserId) {
        Set<Long> userFriends = findById(userId).getFriends();
        Set<Long> otherUserFriends = findById(otherUserId).getFriends();

        List<User> friendsList = new ArrayList<>();

        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
            return friendsList;
        }

        userFriends.stream()
                .filter(userFriends::contains)
                .peek((Long id) -> friendsList.add(findById(id)));

        return friendsList;
    }

    private void userExistsOrException(final Long Id) {
        if (!users.containsKey(Id)) {
            throw new NotFoundException("User not found");
        }
    }

    private Long getNextId() {
        return id++;
    }
}
