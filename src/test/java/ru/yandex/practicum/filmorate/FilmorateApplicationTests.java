package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
class FilmorateApplicationTests {
    private FilmController filmController;
    private Film validFilm;
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setData() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Film");
        validFilm.setDescription("Film description");
        validFilm.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));
        validFilm.setDuration(100L);

        userController = new UserController();
        validUser = new User();
        validUser.setEmail("user@email.ru");
        validUser.setLogin("user333");
        validUser.setName("User User");
        validUser.setBirthday(LocalDate.of(1980, Month.AUGUST, 1));
    }

    /// Film
    @Test
    void createFilm_ShouldSucceed_AllFieldsWalid() {
        Film newFilm = filmController.create(validFilm);

        assertDoesNotThrow(() -> filmController.create(validFilm));
        assertNotNull(newFilm.getId());
        assertEquals(validFilm.getName(), newFilm.getName());
        assertEquals(validFilm.getDescription(), newFilm.getDescription());
        assertEquals(validFilm.getReleaseDate(), newFilm.getReleaseDate());
        assertEquals(validFilm.getDuration(), newFilm.getDuration());
    }

    @Test
    void createFilm_ShouldNotSucceed_BlankName() {
        validFilm.setName("");
        Film invalidFilm = validFilm;

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    void createFilm_ShouldNotSucceed_NullName() {
        validFilm.setName(null);
        Film invalidFilm = validFilm;

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    void createFilm_ShouldNotSucceed_LongDescription() {
        validFilm.setDescription("a".repeat(201));
        Film invalidFilm = validFilm;

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("Mаксимальная длина описания — 200 символов", ex.getMessage());
    }

    @Test
    void createFilm_ShouldNotSucceed_InvalidReleaseDate() {
        validFilm.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));
        Film invalidFilm = validFilm;

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("Дата релза не может быть раньше 28 декабря 1895 года", ex.getMessage());
    }

    @Test
    void createFilm_ShouldNotSucceed_InvalidDuration() {
        validFilm.setDuration(0L);
        Film invalidFilm = validFilm;

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", ex.getMessage());
    }

    @Test
    void updateFilm_ShouldSucceed_AllFieldsWalid() {
        Film oldFilm = filmController.create(validFilm);
        Film newFilm = new Film();
        newFilm.setId(oldFilm.getId());
        newFilm.setName("Film 2");
        newFilm.setDescription("Film description 2");
        newFilm.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 28));
        newFilm.setDuration(120L);

        Film result = filmController.update(newFilm);

        assertEquals(oldFilm.getId(), result.getId());
        assertEquals(result.getName(), newFilm.getName());
        assertEquals(result.getDescription(), newFilm.getDescription());
        assertEquals(result.getReleaseDate(), newFilm.getReleaseDate());
        assertEquals(result.getDuration(), newFilm.getDuration());
    }

    @Test
    void updateFilm_ShouldNotSucceed_InvalidID() {
        Film newFilm = new Film();
        newFilm.setName("Film 2");
        newFilm.setDescription("Film description 2");
        newFilm.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 28));
        newFilm.setDuration(120L);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.update(newFilm));

        assertEquals("Id должен быть указан", ex.getMessage());
    }

    @Test
    void updateFilm_ShouldNotSucceed_InvalidField() {
        Film oldFilm = filmController.create(validFilm);
        Film newFilm = new Film();
        newFilm.setId(oldFilm.getId());
        newFilm.setName("Film 2");
        newFilm.setDescription("Film description 2");
        newFilm.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 28));
        newFilm.setDuration(-200L);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.update(newFilm));

        assertEquals("Ошибка в заполнении полей", ex.getMessage());
    }

    /// User
    @Test
    void createUser_ShouldSucceed_AllFieldsWalid() {
        assertDoesNotThrow(() -> userController.create(validUser));

        userController = new UserController();
        User newUser = userController.create(validUser);

        assertNotNull(newUser.getId());
        assertEquals(newUser.getEmail(), validUser.getEmail());
        assertEquals(newUser.getName(), validUser.getName());
        assertEquals(newUser.getLogin(), validUser.getLogin());
        assertEquals(newUser.getBirthday(), validUser.getBirthday());
    }

    @Test
    void createUser_ShouldNotSucceed_DuplicatedUser() {
        userController.create(validUser);
        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(validUser));
        assertEquals("Пользователь с такой почтой уже зарегестрирован", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_BlankEmail() {
        validUser.setEmail("");
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Почта не может быть пустой и должно содержать @", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_NullEmail() {
        validUser.setEmail(null);
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Почта не может быть пустой и должно содержать @", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_InvalidEmail() {
        validUser.setEmail("example.email.ru");
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Почта не может быть пустой и должно содержать @", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_BlankLogin() {
        validUser.setLogin("");
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_NullLogin() {
        validUser.setLogin(null);
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_ShouldNotSucceed_InvalidLogin() {
        validUser.setLogin("nhdb ki");
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_ShouldSucceed_BlankName() {
        validUser.setName("");
        User newUser = userController.create(validUser);

        assertEquals(newUser.getName(), newUser.getLogin());
    }

    @Test
    void createUser_ShouldSucceed_NullName() {
        validUser.setName(null);
        User newUser = userController.create(validUser);

        assertEquals(newUser.getName(), newUser.getLogin());
    }

    @Test
    void createUser_ShouldNotSucceed_InvalidBirthday() {
        LocalDate now = LocalDate.now();
        validUser.setBirthday(now.plusDays(1));
        User invalidUser = validUser;

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(invalidUser));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }

    @Test
    void updateUser_ShouldSucceed_AllFieldsWalid() {
        User oldUser = userController.create(validUser);
        User newUser = new User();
        newUser.setId(oldUser.getId());
        newUser.setEmail("example2@email.ru");
        newUser.setLogin("user222");
        newUser.setName("User User2");
        newUser.setBirthday(LocalDate.of(2020, Month.JULY, 1));

        User result = userController.update(newUser);

        assertEquals(oldUser.getId(), result.getId());
        assertEquals(oldUser.getEmail(), result.getEmail());
        assertEquals(oldUser.getLogin(), result.getLogin());
        assertEquals(oldUser.getName(), result.getName());
        assertEquals(oldUser.getBirthday(), result.getBirthday());
    }

    @Test
    void updateUser_ShouldNotSucceed_InvalidID() {
        User newUser = new User();
        newUser.setEmail("example2@email.ru");
        newUser.setLogin("user222");
        newUser.setName("User User2");
        newUser.setBirthday(LocalDate.of(2020, Month.JULY, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.update(newUser));

        assertEquals("Id должен быть указан", ex.getMessage());
    }

    @Test
    void updateUser_ShouldNotSucceed_InvalidField() {
        User oldUser = userController.create(validUser);
        User newUser = new User();
        newUser.setId(oldUser.getId());
        newUser.setEmail("example2.email.ru");
        newUser.setLogin("user222");
        newUser.setName("User User2");
        newUser.setBirthday(LocalDate.of(2020, Month.JULY, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.update(newUser));

        assertEquals("Ошибка в заполнении полей", ex.getMessage());
    }

    @Test
    void updateUser_ShouldNotSucceed_UserNotFound() {
        User oldUser = userController.create(validUser);
        User newUser = new User();
        newUser.setId(oldUser.getId() + 1);
        newUser.setEmail("example2@email.ru");
        newUser.setLogin("user222");
        newUser.setName("User User2");
        newUser.setBirthday(LocalDate.of(2020, Month.JULY, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.update(newUser));

        assertEquals("Пользователь не найден", ex.getMessage());
    }
}
