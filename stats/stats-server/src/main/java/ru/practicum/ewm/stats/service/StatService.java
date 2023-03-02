package ru.practicum.ewm.stats.service;

import dto.HitEndpointDto;
import dto.StatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.model.StatsMapper;
import ru.practicum.ewm.stats.model.HitEndpoint;
import ru.practicum.ewm.stats.repo.StatsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatService {
    private final StatsRepository statRepository;

    public HitEndpointDto saveStat(HitEndpoint endpointHit) {
        log.info("Сервис: Получен запрос на сохранение информации об обращении к эндпоинту {}", endpointHit.getUri());
        return StatsMapper.INSTANCE.toEndpointHitDto(statRepository.save(endpointHit));
    }

    public List<StatDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Контроллер: Получен запрос на получение статистики");
        if (uris == null || uris.isEmpty()) {
            return new ArrayList<>();
        } else {
            if (unique) {
                return statRepository.findStatWithUnique(uris, start, end)
                        .stream().sorted(Comparator.comparing(StatDto::getHits).reversed()).collect(Collectors.toList());
            } else {
                return statRepository.findStatNOtUnique(uris, start, end)
                        .stream().sorted(Comparator.comparing(StatDto::getHits).reversed()).collect(Collectors.toList());
            }
        }
    }
}