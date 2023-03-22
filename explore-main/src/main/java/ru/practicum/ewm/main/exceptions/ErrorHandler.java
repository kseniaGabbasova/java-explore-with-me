package ru.practicum.ewm.main.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException exception) {
        return Map.of(
                "message", exception.getMessage(),
                "reason", exception.getReason(),
                "status", "NOT_FOUND",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException exception) {
        return Map.of(
                "message", exception.getMessage(),
                "reason", exception.getReason(),
                "status", "NOT_FOUND",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflictException(ConflictException exception) {
        return Map.of(
                "message", exception.getMessage(),
                "reason", exception.getReason(),
                "status", "NOT_FOUND",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
