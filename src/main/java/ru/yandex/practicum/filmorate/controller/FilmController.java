package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final String NAME_ERROR = "Название не может быть пустым";
    private static final String ID_ERROR = "Id должен быть указан";
    private static final String DESCRIPTION_LENGTH_ERROR = "Mаксимальная длина описания — 200 символов";
    private static final String RELEASE_DATE_ERROR = "Дата релза не может быть раньше 28 декабря 1895 года";
    private static final String DURATION_ERROR = "Продолжительность фильма должна быть положительным числом";
    private static final String FIELDS_ERROR = "Ошибка в заполнении полей";
    private static final String FILM_NOT_FOUND_ERROR = "Фильм не найден";

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Создание нового фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Неверный формат названия");
            throw new ValidationException(NAME_ERROR);
        }
        if (film.getDescription().length() > 200) {
            log.warn("Невреная длина описания");
            throw new ValidationException(DESCRIPTION_LENGTH_ERROR);
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Неверная дата релиза");
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
        if (film.getDuration() < 1) {
            log.warn("Неверная длина фильма");
            throw new ValidationException(DURATION_ERROR);
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм создан");
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Обновление данных фильма");
        if (newFilm.getId() == null) {
            log.warn("ID не передан");
            throw new ValidationException(ID_ERROR);
        }
        if (films.containsKey(newFilm.getId())) {
            if ((newFilm.getName() == null || newFilm.getName().isBlank() ||
                    (newFilm.getDescription() == null || newFilm.getDescription().isBlank()) ||
                    (newFilm.getReleaseDate() == null || newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) ||
                    (newFilm.getDuration() == null || newFilm.getDuration() < 1))) {
                log.warn("Ошибка заполнения полей");
                throw new ValidationException(FIELDS_ERROR);
            }
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Данные фильма обновлены");
            return oldFilm;
        }
        log.warn("Фильм не найден");
        throw new ValidationException(FILM_NOT_FOUND_ERROR);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получение всех фильмов");
        return films.values();
    }
}
