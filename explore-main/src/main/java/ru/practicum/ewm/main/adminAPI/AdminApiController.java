package ru.practicum.ewm.main.adminAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.category.model.CategoryDto;
import ru.practicum.ewm.main.category.model.ShortCategoryDto;
import ru.practicum.ewm.main.category.service.CategoryService;
import ru.practicum.ewm.main.compilation.CompilationService;
import ru.practicum.ewm.main.compilation.model.NewCompilationDto;
import ru.practicum.ewm.main.compilation.model.UpdateCompilationDto;
import ru.practicum.ewm.main.event.model.AdminEventDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.service.EventService;
import ru.practicum.ewm.main.user.model.UserDto;
import ru.practicum.ewm.main.user.service.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin")
public class AdminApiController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @PostMapping("/users")
    public ResponseEntity<Object> createUserByAdmin(@RequestBody UserDto user) {
        log.info("Создание пользователя {}", user.getName());
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Object> deleteUserByAdmin(@Positive @PathVariable("userId") Long id) {
        log.info("Удаление пользователя id {}", id);
        userService.deleteUser(id);
        return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        log.info(MessageFormat.format("Получение списка пользоветелей id: {0} с {1} пользователя и размером страницы {2}",
                ids, from, size));
        return new ResponseEntity<>(userService.getUsers(ids, from, size), HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<Object> createCategory(@RequestBody CategoryDto category) {
        log.info("Создание категории {}", category.getName());
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @DeleteMapping("/categories/{catId}")
    public ResponseEntity<Object> deleteCategoryByAdmin(@Positive @PathVariable("catId") Long id) {
        log.info("Удаление Категории id {}", id);
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/categories/{catId}")
    public ResponseEntity<Object> patchCategory(@PathVariable("catId") Long id, @RequestBody ShortCategoryDto updatingDto) {
        log.info("Обновлеие данных категории id {}", id);
        return new ResponseEntity<>(categoryService.update(id, updatingDto), HttpStatus.OK);
    }

    @PatchMapping("/events/{eventId}")
    public ResponseEntity<Object> updateEventByAdmin(@PathVariable Long eventId,
                                                     @RequestBody AdminEventDto eventUpdateDto) {
        log.info("Изменение действия над событием {}", eventId);
        return new ResponseEntity<>(eventService.updateEventsByAdmin(eventId, eventUpdateDto), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<Object> getEvents(@RequestParam(required = false) List<Long> users,
                                            @RequestParam(required = false) List<Event.State> states,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                            LocalDateTime rangeEnd,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение всех событий admin");
        return new ResponseEntity<>(eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size),
                HttpStatus.OK);
    }

    @PostMapping("/compilations")
    public ResponseEntity<Object> createCompilations(@RequestBody NewCompilationDto newCompilationDto) {
        log.info("Создание подборки {}", newCompilationDto.getTitle());
        return new ResponseEntity<>(compilationService.createCompilation(newCompilationDto), HttpStatus.CREATED);
    }

    @PatchMapping("/compilations/{compId}")
    public ResponseEntity<Object> updateCompilationByAdmin(@Positive @PathVariable Long compId,
                                                           @RequestBody UpdateCompilationDto updatingCompilationDto) {
        log.info("Обновление подборки {}", compId);
        return new ResponseEntity<>(compilationService.updateCompilation(compId, updatingCompilationDto), HttpStatus.OK);
    }

    @DeleteMapping("/compilations/{compId}")
    public ResponseEntity<Object> deleteCompilationByAdmin(@Positive @PathVariable Long compId) {
        log.info("Удаление подборки {}", compId);
        compilationService.deleteCompilationById(compId);
        return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
    }
}
