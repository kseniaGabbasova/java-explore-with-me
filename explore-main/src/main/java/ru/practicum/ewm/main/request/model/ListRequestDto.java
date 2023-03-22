package ru.practicum.ewm.main.request.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListRequestDto {
    List<RequestDto> confirmedRequests;

    List<RequestDto> rejectedRequests;
}
