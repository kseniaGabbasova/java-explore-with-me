package ru.practicum.ewm.main.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RootException extends RuntimeException{
    private final String message;
    private final String reason;
    private final LocalDateTime timestamp;

    public RootException(String message, String reason, LocalDateTime timestamp) {
        this.message = message;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}
