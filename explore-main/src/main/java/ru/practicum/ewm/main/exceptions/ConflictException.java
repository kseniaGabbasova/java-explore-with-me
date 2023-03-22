package ru.practicum.ewm.main.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConflictException extends RootException {
    public ConflictException(String message, String reason, LocalDateTime timestamp) {
        super(message, reason, timestamp);
    }
}
