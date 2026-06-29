package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    private final String nameError = "Название не может быть пустым";
    private final String idError = "Id должен быть указан";
    private final String descriptionLengthError = "Mаксимальная длина описания — 200 символов";
    private final String releaseDateError = "Дата релза не может быть раньше 28 декабря 1895 года";
    private final String durationError = "Продолжительность фильма должна быть положительным числом";
    private final String fieldsError = "Ошибка в заполнении полей";
    private final String filmNotFoundError = "Фильм не найден";

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Создание нового фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Неверный формат названия");
            throw new ValidationException(nameError);
        }
        if (film.getDescription().length() > 200) {
            log.warn("Невреная длина описания");
            throw new ValidationException(descriptionLengthError);
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Неверная дата релиза");
            throw new ValidationException(releaseDateError);
        }
        if (film.getDuration() < 1) {
            log.warn("Неверная длина фильма");
            throw new ValidationException(durationError);
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
            throw new ValidationException(idError);
        }
        if (films.containsKey(newFilm.getId())) {
            if ((newFilm.getName() == null || newFilm.getName().isBlank() ||
                    (newFilm.getDescription() == null || newFilm.getDescription().isBlank()) ||
                    (newFilm.getReleaseDate() == null || newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) ||
                    (newFilm.getDuration() == null || newFilm.getDuration() < 1))) {
                log.warn("Ошибка заполнения полей");
                throw new ValidationException(fieldsError);
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
        throw new ValidationException(filmNotFoundError);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получение всех фильмов");
        return films.values();
    }
}
