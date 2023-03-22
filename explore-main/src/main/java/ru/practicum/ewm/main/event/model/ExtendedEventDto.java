package ru.practicum.ewm.main.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedEventDto {
    private Long id;
    private String annotation;

    private Category category;
    private int confirmedRequests;
    private LocalDateTime createdOn;

    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private User initiator;
    private Location location;

    private boolean paid;

    private int participantLimit;

    private boolean available;

    private LocalDateTime publishedOn;

    private boolean requestModeration;

    private Event.State state;

    private String title;
    private Long views;
}
