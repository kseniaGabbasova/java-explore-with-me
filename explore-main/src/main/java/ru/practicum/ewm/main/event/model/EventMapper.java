package ru.practicum.ewm.main.event.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.main.category.model.CategoryMapper;
import ru.practicum.ewm.main.user.model.UserMapper;

@Mapper(componentModel = "spring",
        uses = {
                UserMapper.class,
                CategoryMapper.class
        },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "category.id", source = "category")
    Event toEvent(NewEventDto newEventDto);

    CompilationEventDto toCompilationDtoFromFull(ExtendedEventDto eventFullDto);

    ShortEventDto toEventShortDto(Event event);

    EventDto toEventDto(Event event);

    ExtendedEventDto toEventFullDto(Event event);

    Location toEntity(Location location);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event updateEventWithUser(UpdateEventDto eventUpdateDto, @MappingTarget Event event);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event updateEventWithUser(AdminEventDto eventUpdateAdminDto, @MappingTarget Event stored);
}
