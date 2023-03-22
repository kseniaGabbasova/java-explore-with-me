package ru.practicum.ewm.main.compilation;

import com.querydsl.core.BooleanBuilder;
import dto.StatDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.compilation.model.*;
import ru.practicum.ewm.main.compilation.repo.CompilationRepository;
import ru.practicum.ewm.main.event.model.CompilationEventDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventMapper;
import ru.practicum.ewm.main.event.model.ExtendedEventDto;
import ru.practicum.ewm.main.event.repo.EventRepository;
import ru.practicum.ewm.main.exceptions.NotFoundException;
import ru.practicum.ewm.main.exceptions.ValidationException;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.repo.RequestRepository;
import client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompilationService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CompilationRepository compilationRepository;
    DateTimeFormatter returnedTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsClient statClient;

    @Autowired
    public CompilationService(EventRepository eventRepository,
                              RequestRepository requestRepository,
                              CompilationRepository compilationRepository,
                              @Value("${stat-server.url}") String url,
                              @Value("${application.name}") String appName) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.compilationRepository = compilationRepository;
        this.statClient = new StatsClient(url, appName, new RestTemplateBuilder());
    }

    public void deleteCompilationById(Long compId) {
        log.info("Удаление подборки admin");
        compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или недоступен"));
        compilationRepository.deleteById(compId);
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки admin");
        if (newCompilationDto.getTitle() == null) {
            throw new ValidationException("Пустой заголовок");
        }
        List<Event> storedEvents = eventRepository.findAllByEvents(newCompilationDto.getEvents());
        Compilation compilation = new Compilation(null, storedEvents, newCompilationDto.isPinned(), newCompilationDto.getTitle());
        Compilation saved = compilationRepository.save(compilation);
        return createCompilationDto(saved);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updatingCompilationDto) {
        log.info("Обновление подборки подборки admin");
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        Compilation newCompilation = createCompilationForUpdate(compilation, updatingCompilationDto);
        compilationRepository.save(newCompilation);
        return createCompilationDto(newCompilation);
    }


    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки по id {}", compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        return createCompilationDto(compilation);
    }

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение всех подборок с пагинацией и привзкой {}", pinned);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        QCompilation qCompilation = QCompilation.compilation;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qCompilation.pinned.eq(true));
        if (pinned != null) {
            booleanBuilder.and(qCompilation.pinned.eq(pinned));
        }
        return compilationRepository.findAll(Objects.requireNonNull(booleanBuilder.getValue()),
                        pageable).getContent()
                .stream()
                .map(this::createCompilationDto)
                .collect(Collectors.toList());
    }

    private Compilation createCompilationForUpdate(Compilation stored, UpdateCompilationDto updatingCompilationDto) {
        if (updatingCompilationDto.getPinned() != null) {
            stored.setPinned(updatingCompilationDto.getPinned());
        }
        if (updatingCompilationDto.getTitle() != null) {
            stored.setTitle(updatingCompilationDto.getTitle());
        }
        if (updatingCompilationDto.getEvents() != null) {
            stored.setEvents(eventRepository.findAllByEvents(updatingCompilationDto.getEvents()));
        }
        return stored;
    }

    private CompilationDto createCompilationDto(Compilation compilation) {
        List<ExtendedEventDto> eventFullDtoList = compilation.getEvents()
                .stream()
                .map(EventMapper.INSTANCE::toEventFullDto)
                .collect(Collectors.toList());
        List<CompilationEventDto> eventFullDtoListWithViews = eventFullDtoList
                .stream()
                .map(this::preparingFullDtoWithStat)
                .collect(Collectors.toList());
        return new CompilationDto(eventFullDtoListWithViews, compilation.getId(),
                compilation.isPinned(), compilation.getTitle());
    }

    private CompilationEventDto preparingFullDtoWithStat(ExtendedEventDto eventFullDto) {
        List<StatDto> stat =
                statClient.getStat(eventFullDto.getCreatedOn().format(returnedTimeFormat),
                        LocalDateTime.now().format(returnedTimeFormat),
                        List.of("/events/" + eventFullDto.getId()), false).getBody();
        if (stat.size() > 0) {
            eventFullDto.setViews(stat.get(0).getHits());
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndAndEvent_Id(Request.RequestStatus.CONFIRMED,
                eventFullDto.getId());
        eventFullDto.setConfirmedRequests(confirmedRequests.size());
        return EventMapper.INSTANCE.toCompilationDtoFromFull(eventFullDto);
    }
}
