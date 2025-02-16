package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.contracts.MpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public List<Mpa> getList() {
        return mpaRepository.getAll();
    }

    public Mpa getById(Long id) {

        Mpa mpa = mpaRepository.findById(id);

        if (mpa == null) {
            throw new NotFoundException("Mpa not found");
        }

        return mpa;
    }
}
