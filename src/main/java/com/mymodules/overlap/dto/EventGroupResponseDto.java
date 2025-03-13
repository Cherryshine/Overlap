package com.mymodules.overlap.dto;

import lombok.*;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EventGroupResponseDto {

    private String username;
    private String title;
    private List<LocalDate> dates;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean creatorParticipates;
    private List<SelectedTime> creatorSelectedTimes;
}
