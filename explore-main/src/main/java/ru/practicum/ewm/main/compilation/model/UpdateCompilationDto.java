package ru.practicum.ewm.main.compilation.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDto {
    private String title;
    private List<Long> events;
    private Boolean pinned;
}
