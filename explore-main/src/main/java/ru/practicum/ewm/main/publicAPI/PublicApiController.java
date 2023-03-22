package ru.practicum.ewm.main.publicAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.category.service.CategoryService;
import ru.practicum.ewm.main.compilation.CompilationService;
import ru.practicum.ewm.main.event.service.EventService;
//import ru.practicum.ewm.main.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
public class PublicApiController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @GetMapping("/categories/{catId}")
    public ResponseEntity<Object> get(@PathVariable("catId") Long id) {
        log.info("Получение информации о категории id {}", id);
        return new ResponseEntity<>(categoryService.getCategoryById(id), HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<Object> get(@PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                      @Positive @RequestParam(defaultValue = "10") int size) {
        log.info(MessageFormat.format("Получение списка категорий id: {0} с {1} категории и размером страницы {2}",
                from, size));
        return new ResponseEntity<>(categoryService.getCategories(from, size), HttpStatus.OK);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Object> getEventById(@Positive @PathVariable Long id, HttpServletRequest request) {
        log.info("endpoint path: {}", request.getRequestURI());
        return new ResponseEntity<>(eventService.getEventById(id, request), HttpStatus.OK);
    }

    //фильтр
    @GetMapping("/events")
    public ResponseEntity<Object> getEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false, defaultValue = "EVENT_DATE") FilterSort sort,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size, HttpServletRequest request) {
        log.info("endpoint path: {}", request.getRequestURI());
        return new ResponseEntity<>(eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request),
                HttpStatus.OK);
    }

    @GetMapping("/compilations")
    public ResponseEntity<Object> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение всех подборок привязка {}", pinned);
        return new ResponseEntity<>(compilationService.getAllCompilations(pinned, from, size), HttpStatus.OK);
    }

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<Object> getCompilationById(@PositiveOrZero @PathVariable Long compId) {
        log.info("Получение информации о подборке id {}", compId);
        return new ResponseEntity<>(compilationService.getCompilationById(compId), HttpStatus.OK);
    }

    public enum FilterSort {
        EVENT_DATE, VIEWS
    }
}
