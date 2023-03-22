package ru.practicum.ewm.main.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import dto.StatDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repo.CategoryRepository;
import ru.practicum.ewm.main.event.model.*;
import ru.practicum.ewm.main.event.repo.EventRepository;
import ru.practicum.ewm.main.exceptions.NotFoundException;
import ru.practicum.ewm.main.publicAPI.PublicApiController;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.repo.RequestRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repo.UserRepository;
import ru.practicum.ewm.main.validator.Validator;
import client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final Validator validator;

    DateTimeFormatter returnedTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsClient statClient;

    @Autowired
    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        RequestRepository requestRepository,
                        Validator validator,
                        @Value("${stat-server.url}") String url,
                        @Value("${application.name}") String appName) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.validator = validator;
        statClient = new StatsClient(url, appName, new RestTemplateBuilder());
    }

    public Object createEvent(Long userId, NewEventDto newEvent) {
        validator.validateNewEventDto(newEvent);
        log.info("Создание события в категории {}", newEvent.getCategory());
        User initiator = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        Category stored = categoryRepository.findById(newEvent.getCategory()).orElseThrow(() ->
                new NotFoundException("Категория с id" + newEvent.getCategory() + "не найдена",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        Event newEventEntity = creatingNewEvent(newEvent, initiator, stored);
        return EventMapper.INSTANCE.toEventDto(eventRepository.save(newEventEntity));
    }

    public Object getEventsByUserId(Long userId, int from, int size) {
        log.info("Получение информации о событии пользователем");
        Pageable pageable = PageRequest.of(from / size, size);
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        return eventRepository.getOwnerEvents(userId, pageable).stream()
                .map(EventMapper.INSTANCE::toEventShortDto).collect(Collectors.toList());
    }

    public Object getEventsByUserAndEventId(Long userId, Long eventId) {
        log.info("Получение информации о событии пользователем");
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        Event stored = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id" + eventId + "не найдено",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        return EventMapper.INSTANCE.toEventDto(stored);
    }

    public Object updateEventsByUser(Long userId, Long eventId, UpdateEventDto eventUpdateDto) {
        log.info("Обновление события пользователем");
        Event stored = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id" + eventId + "не найдено",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        validator.updValidationDtoForUser(userId, eventUpdateDto, stored);
        return EventMapper.INSTANCE.toEventDto(eventRepository.save(createUserUpdateEvent(stored, eventUpdateDto)));
    }

    public Object updateEventsByAdmin(Long eventId, AdminEventDto eventUpdateAdminDto) {
        log.info("Обновление события админом");
        Event stored = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id" + eventId + "не найдено",
                        "Запрашиваемый объект не найден или не доступен", LocalDateTime.now()));
        validator.updValidationDtoForAdmin(stored, eventUpdateAdminDto);
        return EventMapper.INSTANCE.toEventDto(eventRepository.save(createAdminUpdateEvent(stored, eventUpdateAdminDto)));
    }

    public Object getEventById(Long id, HttpServletRequest request) {
        log.info("Получение информации пользователем public");
        Event stored;
        try {
            stored = eventRepository.findByIdAndState(id, Event.State.PUBLISHED);
        } catch (RuntimeException runtimeException) {
            throw new NotFoundException("Событие с id" + id + "не найдено",
                    "Запрашиваемый объект не найден или не доступен", LocalDateTime.now());
        }
        statClient.addHit(request);
        ExtendedEventDto eventFullDto = EventMapper.INSTANCE.toEventFullDto(stored);
        stored.setViews(stored.getViews() + 1);
        eventRepository.save(stored);
        return preparingFullDtoWithStat(eventFullDto);
    }

    public Object getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                            LocalDateTime rangeEnd, Boolean onlyAvailable, PublicApiController.FilterSort sort,
                            Integer from, Integer size, HttpServletRequest request) {
        log.info("Получение информации о событиях с фильтрами public");
        statClient.addHit(request);
        Predicate predicate = predicateForUserFilter(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable).getValue();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "eventDate"));
        if (sort.equals(PublicApiController.FilterSort.VIEWS)) {
            pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "views"));
        }
        log.info("Получение информации о событиях с фильтрами public из репозиория");
        List<ExtendedEventDto> eventFullDtoList = eventRepository.findAll(Objects.requireNonNull(predicate), pageable).getContent()
                .stream()
                .map(EventMapper.INSTANCE::toEventFullDto)
                .collect(Collectors.toList());
        return eventFullDtoList.stream().map(this::preparingFullDtoWithStat).collect(Collectors.toList());
    }

    public Object getEventsForAdmin(List<Long> users, List<Event.State> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Получение информации о событиях с фильтрами admin");
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        Predicate predicate = predicateForAdminFilter(users, states, categories, rangeStart, rangeEnd).getValue();
        log.info("Получение информации о событиях с фильтрами admin из репозиория");
        List<ExtendedEventDto> eventFullDtoList = eventRepository.findAll(Objects.requireNonNull(predicate), pageable).getContent()
                .stream()
                .map(EventMapper.INSTANCE::toEventFullDto)
                .collect(Collectors.toList());
        return eventFullDtoList.stream().map(this::preparingFullDtoWithStat).collect(Collectors.toList());
    }

    private Event createAdminUpdateEvent(Event stored, AdminEventDto eventUpdateAdminDto) {
        Event updEventWithoutState = EventMapper.INSTANCE.updateEventWithUser(eventUpdateAdminDto, stored);
        if (eventUpdateAdminDto.getCategory() != null) {
            Category newCategory = categoryRepository.findById(eventUpdateAdminDto.getCategory()).get();
            updEventWithoutState.setCategory(newCategory);
        }
        if (Objects.equals(AdminEventDto.State.PUBLISH_EVENT, eventUpdateAdminDto.getStateAction())) {
            updEventWithoutState.setState(Event.State.PUBLISHED);
            updEventWithoutState.setPublishedOn(LocalDateTime.now());
        }
        if (Objects.equals(AdminEventDto.State.REJECT_EVENT, eventUpdateAdminDto.getStateAction())) {
            updEventWithoutState.setState(Event.State.CANCELED);
        }
        return updEventWithoutState;
    }

    private Event createUserUpdateEvent(Event stored, UpdateEventDto eventUpdateDto) {
        Event updEventWithoutState = EventMapper.INSTANCE.updateEventWithUser(eventUpdateDto, stored);
        if (eventUpdateDto.getCategory() != null) {
            Category newCategory = categoryRepository.findById(eventUpdateDto.getCategory()).get();
            updEventWithoutState.setCategory(newCategory);
        }
        if (Objects.equals(UpdateEventDto.State.SEND_TO_REVIEW, eventUpdateDto.getStateAction())) {
            updEventWithoutState.setState(Event.State.PENDING);
        }
        if (Objects.equals(UpdateEventDto.State.CANCEL_REVIEW, eventUpdateDto.getStateAction())) {
            updEventWithoutState.setState(Event.State.CANCELED);
        }
        return updEventWithoutState;
    }

    private BooleanBuilder predicateForAdminFilter(List<Long> users, List<Event.State> states,
                                                   List<Long> categories, LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd) {
        QEvent qEvent = QEvent.event;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (users != null) {
            booleanBuilder.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null) {
            booleanBuilder.and(QEvent.event.state.in(states));
        }
        if (categories != null) {
            booleanBuilder.and(QEvent.event.category.id.in(categories));
        }
        if (rangeStart != null) {
            booleanBuilder.and(qEvent.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            booleanBuilder.and(qEvent.eventDate.before(rangeEnd));
        }
        if (rangeStart == null) {
            booleanBuilder.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        return booleanBuilder;
    }

    private BooleanBuilder predicateForUserFilter(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable) {
        QEvent qEvent = QEvent.event;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qEvent.state.eq(Event.State.PUBLISHED));
        if (text != null) {
            booleanBuilder.and(qEvent.annotation.containsIgnoreCase(text).or(qEvent.description.containsIgnoreCase(text)));
        }
        if (categories != null) {
            booleanBuilder.and(QEvent.event.category.id.in(categories));
        }
        if (paid != null) {
            booleanBuilder.and(QEvent.event.paid.eq(paid));
        }
        if (onlyAvailable != null) {
            booleanBuilder.and(QEvent.event.available.eq(true));
        }
        if (rangeStart != null) {
            booleanBuilder.and(qEvent.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            booleanBuilder.and(qEvent.eventDate.before(rangeEnd));
        }
        if (rangeStart == null) {
            booleanBuilder.and(qEvent.eventDate.after(LocalDateTime.now()));
        }
        return booleanBuilder;
    }

    private ExtendedEventDto preparingFullDtoWithStat(ExtendedEventDto eventFullDto) {
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
        return eventFullDto;
    }

    private Event creatingNewEvent(NewEventDto newEvent, User user, Category category) {
        return new Event(null, newEvent.getAnnotation(), category, LocalDateTime.now(), newEvent.getDescription(),
                newEvent.getEventDate(), user, newEvent.getLocation(), newEvent.getPaid(), newEvent.getParticipantLimit(),
                true, null, newEvent.getRequestModeration(), Event.State.PENDING, newEvent.getTitle(), 0L);
    }
}
