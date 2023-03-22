package ru.practicum.ewm.main.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.user.model.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompilationEventDto {
    private Long id;
    private String annotation;
    private Category category;
    private Long confirmedRequests;
    private User initiator;
    private Boolean paid;
    private String title;
    private Integer views;
}
