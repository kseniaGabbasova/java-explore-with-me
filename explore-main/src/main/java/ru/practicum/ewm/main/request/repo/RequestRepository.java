package ru.practicum.ewm.main.request.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequester_IdAndEvent_Id(Long userId, Long eventId);

    List<Request> findAllByEvent_Id(Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    @Query(value = "SELECT r FROM Request r WHERE r.event.id = :eventId AND r.id IN :requestIds")
    List<Request> findStoredUpdRequests(@Param("eventId") Long eventId, @Param("requestIds") List<Long> ids);

    @Query(value = "SELECT r FROM Request r WHERE r.status = :status AND r.id IN :ids")
    List<Request> findStoredUpdRequestsWithStatus(@Param("status") Request.RequestStatus status, @Param("ids") List<Long> ids);

    List<Request> findAllByStatusAndAndEvent_Id(@Param("status") Request.RequestStatus status, Long id);
}
