package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private static final String EMAIL_FIELD = "email";
    private static final String LOGIN_FIELD = "login";
    private static final String BIRTHDAY_FIELD = "birthday";
    private static final String ID_FIELD = "id";
    private static final String FIELDS = "fields";
    private static final String USER_FIELD = "user";

    private static final String EMAIL_ERROR = "Почта не может быть пустой и должно содержать @";
    private static final String LOGIN_ERROR = "Логин не может быть пустым и содержать пробелы";
    private static final String BIRTHDAY_ERROR = "Дата рождения не может быть в будущем";
    private static final String ID_ERROR = "Id должен быть указан";
    private static final String FIELDS_ERROR = "Ошибка в заполнении полей";
    private static final String USER_NOT_FOUND_ERROR = "Пользователь не найден";
    private static final String DUPLICATED_USER_ERROR = "Пользователь с такой почтой уже зарегестрирован";

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()) {
            throw new ValidationException(EMAIL_FIELD, EMAIL_ERROR);
        }
        if (!users.values().stream()
                .filter(value -> value.getEmail().equals(user.getEmail()))
                .toList().isEmpty()) {
            throw new ValidationException(USER_FIELD, DUPLICATED_USER_ERROR);
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            throw new ValidationException(LOGIN_FIELD, LOGIN_ERROR);
        }
        LocalDate now = LocalDate.now();
        if (user.getBirthday().isAfter(now)) {
            throw new ValidationException(BIRTHDAY_FIELD, BIRTHDAY_ERROR);
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException(ID_FIELD, ID_ERROR);
        }
        LocalDate now = LocalDate.now();
        if (users.containsKey(newUser.getId())) {
            if ((newUser.getEmail() == null || !newUser.getEmail().contains("@")) ||
                    (newUser.getLogin() == null || newUser.getLogin().contains(" ")) ||
                    (newUser.getBirthday().isAfter(now))) {
                throw new ValidationException(FIELDS, FIELDS_ERROR);
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
            return oldUser;
        }
        throw new NotFoundException(USER_FIELD, USER_NOT_FOUND_ERROR);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void delete(User user) {
        users.remove(user.getId());
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
