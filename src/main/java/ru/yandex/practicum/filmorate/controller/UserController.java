package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    private final String emailError = "Почта не может быть пустой и должно содержать @";
    private final String loginError = "Логин не может быть пустым и содержать пробелы";
    private final String birthdayError = "Дата рождения не может быть в будущем";
    private final String idError = "Id должен быть указан";
    private final String fieldsError = "Ошибка в заполнении полей";
    private final String userNotFoundError = "Пользователь не найден";
    private final String duplicatedUserError = "Пользователь с такой почтой уже зарегестрирован";

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Создание нового пользователя");
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()) {
            log.warn("Неверный формат почты");
            throw new ValidationException(emailError);
        }
        if (!users.values().stream()
                .filter(value -> value.getEmail().equals(user.getEmail()))
                .toList().isEmpty()) {
            log.warn("Пользователь уже существует");
            throw new ValidationException(duplicatedUserError);
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.warn("Неверный формат логина");
            throw new ValidationException(loginError);
        }
        LocalDate now = LocalDate.now();
        if (user.getBirthday().isAfter(now)) {
            log.warn("Неверная дата рождения");
            throw new ValidationException(birthdayError);
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
        if (newUser.getId() == null) {
            log.warn("ID не передан");
            throw new ValidationException(idError);
        }
        LocalDate now = LocalDate.now();
        if (users.containsKey(newUser.getId())) {
            if ((newUser.getEmail() == null || !newUser.getEmail().contains("@")) ||
                    (newUser.getLogin() == null || newUser.getLogin().contains(" ")) ||
                    (newUser.getBirthday().isAfter(now))) {
                log.warn("Ошибка заполнения полей");
                throw new ValidationException(fieldsError);
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
        throw new ValidationException(userNotFoundError);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получение данных о всех пользователях");
        return users.values();
    }
}
