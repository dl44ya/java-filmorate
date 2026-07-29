package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private static final String EMAIL_ERROR = "Почта не может быть пустой и должно содержать @";
    private static final String LOGIN_ERROR = "Логин не может быть пустым и содержать пробелы";
    private static final String BIRTHDAY_ERROR = "Дата рождения не может быть в будущем";
    private static final String ID_ERROR = "Id должен быть указан";
    private static final String FIELDS_ERROR = "Ошибка в заполнении полей";
    private static final String USER_NOT_FOUND_ERROR = "Пользователь не найден";
    private static final String DUPLICATED_USER_ERROR = "Пользователь с такой почтой уже зарегестрирован";

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Создание нового пользователя");
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()) {
            log.warn("Неверный формат почты");
            throw new ValidationException(EMAIL_ERROR);
        }
        if (!users.values().stream()
                .filter(value -> value.getEmail().equals(user.getEmail()))
                .toList().isEmpty()) {
            log.warn("Пользователь уже существует");
            throw new ValidationException(DUPLICATED_USER_ERROR);
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.warn("Неверный формат логина");
            throw new ValidationException(LOGIN_ERROR);
        }
        LocalDate now = LocalDate.now();
        if (user.getBirthday().isAfter(now)) {
            log.warn("Неверная дата рождения");
            throw new ValidationException(BIRTHDAY_ERROR);
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Новый пользователь создан");
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Обновлене данных пользоваеля");
        log.info("Получен id: " + newUser.getId());
        log.info("Все id: " + users.keySet());
        if (newUser.getId() == null) {
            log.warn("ID не передан");
            throw new ValidationException(ID_ERROR);
        }
        LocalDate now = LocalDate.now();
        if (users.containsKey(newUser.getId())) {
            if ((newUser.getEmail() == null || !newUser.getEmail().contains("@")) ||
                    (newUser.getLogin() == null || newUser.getLogin().contains(" ")) ||
                    (newUser.getBirthday().isAfter(now))) {
                log.warn("Ошибка заполнения полей");
                throw new ValidationException(FIELDS_ERROR);
            }
            User oldUser = users.get(newUser.getId());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
            log.info("Данные пользователя обновлены");
            return oldUser;
        }
        log.warn("Пользователь не найден");
        throw new ValidationException(USER_NOT_FOUND_ERROR);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получение данных о всех пользователях");
        return users.values();
    }
}
