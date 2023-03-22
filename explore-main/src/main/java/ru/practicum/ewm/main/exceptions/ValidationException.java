package ru.practicum.ewm.main.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ValidationException extends RootException {
    public ValidationException(String message, String reason, LocalDateTime timestamp) {
        super(message, reason, timestamp);
    }
}
