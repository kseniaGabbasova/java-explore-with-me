package ru.practicum.ewm.stats;

import dto.HitEndpointDto;
import dto.StatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.model.HitEndpoint;
import ru.practicum.ewm.stats.model.StatsMapper;
import ru.practicum.ewm.stats.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping
public class StatsController {
    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<HitEndpointDto> saveStat(@RequestBody @Valid HitEndpointDto endpointHit) {
        log.info("Контроллер: Получен запрос на сохрание информации об обращении к эндпоинту {}", endpointHit.getUri());
        HitEndpoint endpointHit1 = StatsMapper.INSTANCE.toEndpointHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).body(statService.saveStat(endpointHit1));
    }

    @GetMapping("/stats")
    public List<StatDto> getStat(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                     LocalDateTime start,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                 LocalDateTime end,
                                 @RequestParam List<String> uris,
                                 @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Контроллер: Получен запрос на получение статистики");
        return statService.getStat(start, end, uris, unique);
    }
}
