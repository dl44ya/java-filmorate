package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

public class NotFoundException extends RuntimeException {
    @Getter
    private final String parameter;

    public NotFoundException(String parameter, String message) {
        super(message);
        this.parameter = parameter;
    }
}
