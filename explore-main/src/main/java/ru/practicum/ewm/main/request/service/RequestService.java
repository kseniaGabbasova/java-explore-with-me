package ru.practicum.ewm.main.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.repo.EventRepository;
import ru.practicum.ewm.main.exceptions.ConflictException;
import ru.practicum.ewm.main.exceptions.NotFoundException;
import ru.practicum.ewm.main.request.model.*;
import ru.practicum.ewm.main.request.repo.RequestRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public Object createRequest(long userId, long eventId) {
        log.debug("Получен запрос на создание запроса на участие пользователя {}", userId);
        Event stored = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        List<Request> requests = requestRepository.findAllByRequester_IdAndEvent_Id(userId, eventId);
        checkRequest(userId, stored, requests);
        return RequestMapper.INSTANCE.toRequestDto(requestRepository.save(creatingRequest(userId, stored)));
    }

    public Object updCancelStatus(Long userId, Long requestId) {
        log.debug("Получен запрос на изменение статуса запроса на участие пользователя {}", userId);
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        Request requestStored = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        requestStored.setStatus(Request.RequestStatus.CANCELED);
        return RequestMapper.INSTANCE.toRequestDto(requestRepository.save(requestStored));
    }

    public Object getAllRequestsForUser(Long userId) {
        log.debug("Получение всех запросов пользователя {}", userId);
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        List<Request> storedRequests = requestRepository.findAllByRequesterId(userId);
        return storedRequests
                .stream()
                .map(RequestMapper.INSTANCE::toRequestDto)
                .collect(Collectors.toList());
    }

    public Object getAllRequestsByEventId(Long eventId, Long userId) {
        eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        List<Request> storedRequests =
                requestRepository.findAllByEvent_Id(eventId);
        return storedRequests.stream().map(RequestMapper.INSTANCE::toRequestDto).collect(Collectors.toList());
    }

    public ListRequestDto updateRequestsStatusForEvent(Long eventId, Long userId, RequestDtoWithStatus dto) {
        Event storedEvent = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        List<Request> requestsForUpdate = requestRepository.findStoredUpdRequests(eventId, dto.getRequestIds());
        checkRequestsListForUpdate(dto.getStatus(), storedEvent, requestsForUpdate);
        eventRepository.save(storedEvent);
        return createRequestListDto(dto.getRequestIds());
    }

    private ListRequestDto createRequestListDto(List<Long> idRequests) {
        List<RequestDto> confirmedRequests = requestRepository.findStoredUpdRequestsWithStatus(Request.RequestStatus.CONFIRMED,
                        idRequests)
                .stream()
                .map(RequestMapper.INSTANCE::toRequestDto)
                .collect(Collectors.toList());
        List<RequestDto> rejectedRequests = requestRepository.findStoredUpdRequestsWithStatus(Request.RequestStatus.REJECTED,
                        idRequests)
                .stream()
                .map(RequestMapper.INSTANCE::toRequestDto)
                .collect(Collectors.toList());
        return new ListRequestDto(confirmedRequests, rejectedRequests);
    }

    private void checkRequestsListForUpdate(Request.RequestStatus newStatus,
                                            Event storedEvent, List<Request> requestsForUpdate) {
        for (Request request : requestsForUpdate) {
            if (storedEvent.getParticipantLimit() == 0) {
                request.setStatus(Request.RequestStatus.REJECTED);
                requestRepository.save(request);
                throw new ConflictException("Нет свободных мест в событиии");
            }
            if (!request.getStatus().equals(Request.RequestStatus.PENDING)) {
                throw new ConflictException("Обновление невозможно для статуса" + Request.RequestStatus.PENDING);
            }
            if (newStatus.equals(Request.RequestStatus.CONFIRMED)) {
                request.setStatus(Request.RequestStatus.CONFIRMED);
                requestRepository.save(request);
                storedEvent.setParticipantLimit(storedEvent.getParticipantLimit() - 1);
            }
            if (newStatus.equals(Request.RequestStatus.REJECTED)) {
                request.setStatus(Request.RequestStatus.REJECTED);
                requestRepository.save(request);
            }
        }
    }

    private void checkRequest(long userId, Event stored, List<Request> requests) {
        if (requests.size() != 0) {
            throw new ConflictException("Нельзя повторно отправлять запрос на участие");
        }
        if (stored.getInitiator().getId() == userId) {
            throw new ConflictException("Нельзя ходить на свои мероприятия как гость");
        }
        if (!stored.getState().equals(Event.State.PUBLISHED)) {
            throw new ConflictException("Нельзя подать запрос на неопубликованное событие");
        }
        if (stored.getParticipantLimit() == 0) {
            throw new ConflictException("Нет свободных мест в событиии");
        }
    }

    private Request creatingRequest(Long userId, Event stored) {
        Request request = new Request();
        if (!stored.isRequestModeration()) {
            request.setStatus(Request.RequestStatus.CONFIRMED);
            stored.setParticipantLimit(stored.getParticipantLimit() - 1);
            eventRepository.save(stored);
        } else {
            request.setStatus(Request.RequestStatus.PENDING);
        }
        User requester = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        request.setRequester(requester);
        request.setEvent(stored);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
