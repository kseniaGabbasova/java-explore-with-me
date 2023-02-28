package ru.practicum.ewm.stats.model;

import dto.HitEndpointDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StatsMapper {
    StatsMapper INSTANCE = Mappers.getMapper(StatsMapper.class);

    HitEndpointDto toEndpointHitDto(HitEndpoint endpointHit);

    HitEndpoint toEndpointHit(HitEndpointDto endpointHitDto);
}