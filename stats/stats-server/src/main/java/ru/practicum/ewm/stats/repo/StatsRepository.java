package ru.practicum.ewm.stats.repo;

import dto.StatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.model.HitEndpoint;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<HitEndpoint, Long> {

    @Query("select new dto.StatDto(e.app, e.uri, count(distinct e.ip)) " +
            "from HitEndpoint e " +
            "where e.timestamp between ?2 and ?3 " +
            "and e.uri in ?1 " +
            "group by e.app, e.uri")
    List<StatDto> findStatWithUnique(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("select new dto.StatDto(e.app, e.uri, count(e.ip)) " +
            "from HitEndpoint e " +
            "where e.timestamp between ?2 and ?3 " +
            "and e.uri in ?1 " +
            "group by e.app, e.uri")
    List<StatDto> findStatNOtUnique(List<String> uris, LocalDateTime start, LocalDateTime end);
}