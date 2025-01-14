package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTests {
    private static final int DESCRIPTION_LIMIT = 200;
    private static final LocalDate RELEASE_MIN_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    private ru.yandex.practicum.filmorate.controller.FilmController controller;

    private static Film makeFilm() {
        return Film.builder()
                .name(generateString(20))
                .description(generateString(120))
                .duration(100)
                .releaseDate(LocalDate.of(2021, 11, 27))
                .build();
    }

    private static String generateString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    @Test
    public void filmAddedWithValidFieldValues() {
        int filmsCountBefore = controller.list().size();

        assertDoesNotThrow(() -> controller.create(makeFilm()), "Film not created");
        assertNotEquals(filmsCountBefore, controller.list(), "Film not added");
    }

    @Test
    public void filmUpdatedWithValidFieldValues() {
        Film film = controller.create(makeFilm());
        final Film[] films = {null};

        film.setName("Film 2");
        film.setDescription("Film description 2");
        film.setDuration(200);
        film.setReleaseDate(LocalDate.of(2021, 11, 28));

        assertDoesNotThrow(() -> films[0] = controller.update(film), "Film not updated");

        assertEquals(films[0], controller.getFilm(film.getId()), "Film not found");
    }

    @Test
    public void errorOnSaveEmptyObject() {
        assertThrows(ValidationException.class, () -> controller.update(null), "Validation on empty object is failed");
    }

    @Test
    public void errorOnSaveInvalidName() {
        Film film = makeFilm();
        Film film2 = makeFilm();

        film.setName("");
        film2.setName(null);

        assertThrows(ValidationException.class, () -> controller.create(film), "Validation on empty name is failed");
        assertThrows(ValidationException.class, () -> controller.create(film2), "Validation on null name is failed");
    }

    @Test
    public void savedValidName() {
        Film film = makeFilm();

        assertDoesNotThrow(() -> controller.create(film), "Validation failed");
    }

    @Test
    public void errorOnSaveInvalidDescription() {
        Film film = makeFilm();
        Film film2 = makeFilm();

        film.setDescription(null);
        film2.setDescription(generateString(DESCRIPTION_LIMIT + 1));

        assertThrows(ValidationException.class, () -> controller.create(film), "Validation on null description is failed");
        assertThrows(ValidationException.class, () -> controller.create(film2), "Validation on long description is failed");
    }

    @Test
    public void savedValidDescription() {
        Film film = makeFilm();
        Film film2 = makeFilm();
        Film film3 = makeFilm();

        film.setDescription("");
        film2.setDescription(generateString(DESCRIPTION_LIMIT));
        film3.setDescription(generateString(DESCRIPTION_LIMIT - 1));

        assertDoesNotThrow(() -> controller.create(film), "Validation failed");
        assertDoesNotThrow(() -> controller.create(film2), "Validation failed");
        assertDoesNotThrow(() -> controller.create(film3), "Validation failed");
    }

    @Test
    public void errorOnSaveInvalidReleaseDate() {
        Film film = makeFilm();
        Film film2 = makeFilm();

        film.setReleaseDate(null);
        film2.setReleaseDate(RELEASE_MIN_DATE.minusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(film), "Validation on null release date is failed");
        assertThrows(ValidationException.class, () -> controller.create(film2), "Validation on invalid release date is failed");
    }

    @Test
    public void savedValidReleaseDate() {
        Film film = makeFilm();
        Film film2 = makeFilm();

        film.setReleaseDate(LocalDate.now());
        film2.setReleaseDate(RELEASE_MIN_DATE);

        assertDoesNotThrow(() -> controller.create(film), "Validation failed");
        assertDoesNotThrow(() -> controller.create(film2), "Validation failed");
    }

    @Test
    public void errorOnSaveInvalidDuration() {
        Film film = makeFilm();
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> controller.create(film), "Validation on negative duration is failed");
    }

    @Test
    public void savedValidDuration() {
        Film film = makeFilm();
        Film film2 = makeFilm();

        film.setDuration(new Random().nextInt(100));
        film2.setDuration(0);

        assertDoesNotThrow(() -> controller.create(film), "Validation failed");
        assertDoesNotThrow(() -> controller.create(film2), "Validation failed");
    }
}
