package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private static final String USER_FIELD = "user";
    private static final String FILM_FIELD = "film";

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String FILM_NOT_FOUND = "Фильм не найден";

    @Getter
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_FIELD, FILM_NOT_FOUND));
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));

        Set<Long> filmLikes = film.getLikes();
        filmLikes.add(userId);
        film.setLikes(filmLikes);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_FIELD, FILM_NOT_FOUND));
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(USER_FIELD, USER_NOT_FOUND));

        Set<Long> filmLikes = film.getLikes();
        filmLikes.remove(userId);
        film.setLikes(filmLikes);
    }

    public List<Film> findPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .toList();
    }
}
