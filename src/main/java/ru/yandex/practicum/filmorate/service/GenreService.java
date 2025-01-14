package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.contracts.GenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getList() {
        return genreRepository.getAll();
    }

    public Genre getById(Long id) {
        Genre genre = genreRepository.findById(id);

        if (genre == null) {
            throw new NotFoundException("Genre not found");
        }

        return genre;
    }
}
