package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
class FilmorateApplicationTests {
    private UserController userController;
    private User validUser;

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setData() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
        validFilm = new Film("Film",
                "Film description",
                LocalDate.of(1895, Month.DECEMBER, 28),
                100L);

        userController = new UserController(new UserService(new InMemoryUserStorage()));
        validUser = new User("user@email.ru",
                "user333",
                "User User",
                LocalDate.of(1980, Month.AUGUST, 1));
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
        Film newFilm = new Film("Film 2",
                "Film description 2",
                LocalDate.of(1999, Month.DECEMBER, 28),
                120L);
        newFilm.setId(oldFilm.getId());

        Film result = filmController.update(newFilm);

        assertEquals(oldFilm.getId(), result.getId());
        assertEquals(result.getName(), newFilm.getName());
        assertEquals(result.getDescription(), newFilm.getDescription());
        assertEquals(result.getReleaseDate(), newFilm.getReleaseDate());
        assertEquals(result.getDuration(), newFilm.getDuration());
    }

    @Test
    void updateFilm_ShouldNotSucceed_InvalidID() {
        Film newFilm = new Film("Film 2",
                "Film description 2",
                LocalDate.of(1999, Month.DECEMBER, 28),
                120L);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.update(newFilm));

        assertEquals("Id должен быть указан", ex.getMessage());
    }

    @Test
    void updateFilm_ShouldNotSucceed_InvalidField() {
        Film oldFilm = filmController.create(validFilm);
        Film newFilm = new Film("Film 2",
                "Film description 2",
                LocalDate.of(1999, Month.DECEMBER, 28),
                -200L);
        newFilm.setId(oldFilm.getId());

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.update(newFilm));

        assertEquals("Ошибка в заполнении полей", ex.getMessage());
    }

    /// User
    @Test
    void createUser_ShouldSucceed_AllFieldsWalid() {
        assertDoesNotThrow(() -> userController.create(validUser));

        userController = new UserController(new UserService(new InMemoryUserStorage()));
        User createdUser = userController.create(validUser);

        assertNotNull(createdUser.getId());
        assertEquals(createdUser.getEmail(), validUser.getEmail());
        assertEquals(createdUser.getName(), validUser.getName());
        assertEquals(createdUser.getLogin(), validUser.getLogin());
        assertEquals(createdUser.getBirthday(), validUser.getBirthday());
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
        User newUser = new User("example2@email.ru",
                "user222",
                "User User2",
                LocalDate.of(2020, Month.JULY, 1));
        newUser.setId(oldUser.getId());

        User result = userController.update(newUser);

        assertEquals(oldUser.getId(), result.getId());
        assertEquals(oldUser.getEmail(), result.getEmail());
        assertEquals(oldUser.getLogin(), result.getLogin());
        assertEquals(oldUser.getName(), result.getName());
        assertEquals(oldUser.getBirthday(), result.getBirthday());
    }

    @Test
    void updateUser_ShouldNotSucceed_InvalidID() {
        User newUser = new User("example2@email.ru",
                "user222",
                "User User2",
                LocalDate.of(2020, Month.JULY, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.update(newUser));

        assertEquals("Id должен быть указан", ex.getMessage());
    }

    @Test
    void updateUser_ShouldNotSucceed_InvalidField() {
        User oldUser = userController.create(validUser);
        User newUser = new User("example2email.ru",
                "user222",
                "User User2",
                LocalDate.of(2020, Month.JULY, 1));
        newUser.setId(oldUser.getId());

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.update(newUser));

        assertEquals("Ошибка в заполнении полей", ex.getMessage());
    }

    @Test
    void updateUser_ShouldNotSucceed_UserNotFound() {
        User oldUser = userController.create(validUser);
        User newUser = new User("example2@email.ru",
                "user222",
                "User User2",
                LocalDate.of(2020, Month.JULY, 1));
        newUser.setId(oldUser.getId() + 1);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userController.update(newUser));

        assertEquals("Пользователь не найден", ex.getMessage());
    }
}
