package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final String NAME_FIELD = "name";
    private static final String ID_FIELD = "id";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String RELEASE_DATE_FIELD = "release date";
    private static final String DURATION_FIELD = "duration";
    private static final String FIELDS = "fields";
    private static final String FILM_FIELD = "film";

    private static final String NAME_ERROR = "Название не может быть пустым";
    private static final String ID_ERROR = "Id должен быть указан";
    private static final String DESCRIPTION_LENGTH_ERROR = "Mаксимальная длина описания — 200 символов";
    private static final String RELEASE_DATE_ERROR = "Дата релза не может быть раньше 28 декабря 1895 года";
    private static final String DURATION_ERROR = "Продолжительность фильма должна быть положительным числом";
    private static final String FIELDS_ERROR = "Ошибка в заполнении полей";
    private static final String FILM_NOT_FOUND_ERROR = "Фильм не найден";

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException(NAME_FIELD, NAME_ERROR);
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException(DESCRIPTION_FIELD, DESCRIPTION_LENGTH_ERROR);
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException(RELEASE_DATE_FIELD, RELEASE_DATE_ERROR);
        }
        if (film.getDuration() < 1) {
            throw new ValidationException(DURATION_FIELD, DURATION_ERROR);
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
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

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException(ID_FIELD, ID_ERROR);
        }
        if (films.containsKey(newFilm.getId())) {
            if ((newFilm.getName() == null || newFilm.getName().isBlank() ||
                    (newFilm.getDescription() == null || newFilm.getDescription().isBlank()) ||
                    (newFilm.getReleaseDate() == null || newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) ||
                    (newFilm.getDuration() == null || newFilm.getDuration() < 1))) {
                throw new ValidationException(FIELDS, FIELDS_ERROR);
            }
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException(FILM_FIELD, FILM_NOT_FOUND_ERROR);
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public void delete(Film film) {
        films.remove(film.getId());
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }
}
