package ru.practicum.ewm.main.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(max = 2000, min = 20)
    private String annotation;
    @NotNull
    @Positive
    private Long category;
    @NotBlank
    @Size(max = 7000, min = 20)
    private String description;
    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    @NotNull
    private Boolean paid;
    @NotNull
    @PositiveOrZero
    private Integer participantLimit;
    @NotNull
    private Boolean requestModeration;
    @NotBlank
    @Size(max = 120, min = 3)
    private String title;
}
