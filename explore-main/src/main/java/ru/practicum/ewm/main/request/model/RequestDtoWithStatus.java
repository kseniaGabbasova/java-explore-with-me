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
public class RequestDtoWithStatus {
    private List<Long> requestIds;
    private Request.RequestStatus status;
}
