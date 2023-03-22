package ru.practicum.ewm.main.compilation.model;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.main.category.model.CategoryMapper;
import ru.practicum.ewm.main.event.model.EventMapper;
import ru.practicum.ewm.main.user.model.UserMapper;

@Mapper(componentModel = "spring",
        uses = {
                UserMapper.class,
                CategoryMapper.class,
                EventMapper.class
        },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {
    CompilationMapper INSTANCE = Mappers.getMapper(CompilationMapper.class);

    CompilationDto toCompilationDto(Compilation compilation);
}
