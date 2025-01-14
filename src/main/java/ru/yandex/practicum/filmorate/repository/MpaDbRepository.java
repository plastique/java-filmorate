package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.contracts.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbRepository implements MpaRepository {
    public static final String TABLE_NAME = "mpas";

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper = new MpaRowMapper();

    @Override
    public List<Mpa> getAll() {
        return jdbc.query(
                "SELECT * FROM " + TABLE_NAME,
                mapper
        );
    }

    @Override
    public Mpa findById(final Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT * FROM " + TABLE_NAME + " WHERE id = ?",
                    mapper,
                    id
            );
        } catch (RuntimeException e) {
            return null;
        }
    }
}
