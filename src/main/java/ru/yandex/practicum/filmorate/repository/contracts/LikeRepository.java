package ru.yandex.practicum.filmorate.repository.contracts;

public interface LikeRepository {

    void addLike(final Long id, final Long userId);

    void deleteLike(final Long id, final Long userId);
}
