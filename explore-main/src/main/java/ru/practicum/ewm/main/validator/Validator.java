package ru.practicum.ewm.main.validator;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.category.model.CategoryDto;
import ru.practicum.ewm.main.category.model.ShortCategoryDto;
import ru.practicum.ewm.main.event.model.AdminEventDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.NewEventDto;
import ru.practicum.ewm.main.event.model.UpdateEventDto;
import ru.practicum.ewm.main.exceptions.ConflictException;
import ru.practicum.ewm.main.exceptions.ValidationException;
import ru.practicum.ewm.main.user.model.UserDto;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class Validator {
    LocalDateTime time = LocalDateTime.now();

    public void updValidationDtoForAdmin(Event stored, AdminEventDto eventUpdateAdminDto) {
        if (!Objects.equals(Event.State.PENDING, stored.getState())) {
            throw new ConflictException(
                    "Условия выполнения не соблюдены");
        }
        if (stored.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException(
                    "Неверно указана дата события");
        }
        if (eventUpdateAdminDto.getEventDate() != null) {
            if (eventUpdateAdminDto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ConflictException(
                        "Условия выполнения не соблюдены");
            }
        }
    }

    public void updValidationDtoForUser(Long userId, UpdateEventDto eventUpdateDto, Event stored) {
        if (!stored.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    "Условия выполнения не соблюдены");
        }
        if (stored.getState().equals(Event.State.PUBLISHED)) {
            throw new ConflictException(
                    "Условия выполнения не соблюдены");
        }
        if (eventUpdateDto.getEventDate() != null) {
            if (eventUpdateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException(
                        "Условия выполнения не соблюдены");
            }
        }
        if (stored.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(
                    "Условия выполнения не соблюдены");
        }
        if (stored.getParticipantLimit() == 0) {
            throw new ConflictException("Мест нет");
        }
    }

    public void validateCategory(CategoryDto category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new ValidationException(
                    "Не указано имя категории");
        }
    }

    public void validateCategoryForUpd(ShortCategoryDto category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new ValidationException(
                    "Не указано имя категории");
        }
    }

    public void validateUserDto(UserDto user) {
        if (StringUtils.isBlank(user.getName())) {
            throw new ValidationException(
                    "Не указано имя пользователя");
        }
    }

    public void validateNewEventDto(NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(
                    "Неверно указана дата события");
        }
        if (newEventDto.getCategory() <= 0) {
            throw new ValidationException(
                    "Неверно указана категория указано имя пользователя");
        }
        if (StringUtils.isBlank(newEventDto.getAnnotation())) {
            throw new ValidationException(
                    "Не указана аннотация");
        }
    }

    public void validateUpdateEventDto(UpdateEventDto eventUpdateDto) {
        if (null != eventUpdateDto.getEventDate()) {
            if (eventUpdateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException(
                        "Неверно указана дата события");
            }
        }
    }
}
