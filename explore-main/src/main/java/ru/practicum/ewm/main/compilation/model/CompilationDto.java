package ru.practicum.ewm.main.compilation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.main.event.model.CompilationEventDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private List<CompilationEventDto> events;
    private Long id;
    private boolean pinned;
    private String title;
}