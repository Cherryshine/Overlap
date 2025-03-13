package com.mymodules.overlap.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EventGroupRequestDto {

    private String title;
    private List<LocalDate> dates;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean creatorParticipates;
    private List<SelectedTime> creatorSelectedTimes;

}