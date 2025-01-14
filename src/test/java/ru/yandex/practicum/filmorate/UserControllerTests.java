package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTests {

    @Autowired
    private ru.yandex.practicum.filmorate.controller.UserController controller;
    private static int userIndex = 0;

    private static User makeUser() {
        userIndex++;
        return User.builder()
                .name("Vasiliy Pupkin")
                .login("login" + userIndex)
                .email("v.p.test" + userIndex + "@sample.host")
                .birthday(LocalDate.of(1990, 8, 1))
                .build();
    }

    @Test
    public void userAddedWithValidFieldValues() {
        final User[] users = {null};

        assertDoesNotThrow(() -> users[0] = controller.create(makeUser()), "User not created");
        assertTrue(controller.list().contains(users[0]), "User not added");
    }

    @Test
    public void userUpdatedWithValidFieldValues() {
        User user = controller.create(makeUser());

        user.setName("User 2");
        user.setLogin("another-login2");

        assertDoesNotThrow(() -> controller.update(user), "User not updated");
        assertTrue(controller.list().contains(user), "User not found");
    }

    @Test
    public void errorOnSaveEmptyObject() {
        assertThrows(ValidationException.class, () -> controller.update(null), "Validation on empty object is failed");
    }

    @Test
    public void errorOnSaveInvalidEmail() {
        User user = makeUser();
        User user2 = makeUser();
        User user3 = makeUser();

        user.setEmail("");
        user2.setEmail(null);
        user3.setEmail("some-string.somehost.ru");

        assertThrows(ValidationException.class, () -> controller.create(user), "Validation on empty email is failed");
        assertThrows(ValidationException.class, () -> controller.create(user2), "Validation on null email is failed");
        assertThrows(ValidationException.class, () -> controller.create(user3), "Validation on invalid email is failed");
    }

    @Test
    public void savedValidEmail() {
        User user = makeUser();

        assertDoesNotThrow(() -> controller.create(user), "Validation failed");
    }

    @Test
    public void errorOnSaveInvalidLogin() {
        User user = makeUser();
        User user2 = makeUser();
        User user3 = makeUser();

        user.setLogin(null);
        user2.setLogin("");
        user3.setLogin("so me spa ces");

        assertThrows(ValidationException.class, () -> controller.create(user), "Validation on null login is failed");
        assertThrows(ValidationException.class, () -> controller.create(user2), "Validation on empty login is failed");
        assertThrows(ValidationException.class, () -> controller.create(user3), "Validation on login with spaces is failed");
    }

    @Test
    public void savedValidLogin() {
        User user = makeUser();

        assertDoesNotThrow(() -> controller.create(user), "Validation failed");
    }

    @Test
    public void emptyNameReplacedByLogin() {
        User user = makeUser();
        User user2 = makeUser();

        user.setName(null);
        user2.setName("");

        assertEquals(controller.create(user).getName(), user.getLogin(), "Invalid user name");
        assertEquals(controller.create(user2).getName(), user2.getLogin(), "Invalid user name");
    }

    @Test
    public void errorOnSaveInvalidBirthday() {
        User user = makeUser();
        User user2 = makeUser();
        user.setBirthday(null);
        user2.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user), "Validation on null birthday is failed");
        assertThrows(ValidationException.class, () -> controller.create(user2), "Validation on future birthday is failed");
    }

    @Test
    public void savedValidBirthday() {
        User user = makeUser();

        assertDoesNotThrow(() -> controller.create(user), "Validation failed");
    }
}
