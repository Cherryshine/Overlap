package com.mymodules.overlap.dto;

import com.mymodules.overlap.entity.EventGroupEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Setter
@Getter
public class EventGroupResponseDto {

    private Long id;

        private String title;
        private String startTime;
        private String endTime;
        private List<String> selectDates; // List<String>으로 변경





    public EventGroupResponseDto(EventGroupEntity eventGroup) {
        this.id = eventGroup.getId();
        this.title = eventGroup.getTitle();
        this.startTime = eventGroup.getStartTime();
        this.endTime = eventGroup.getEndTime();
        this.selectDates = eventGroup.getSelectDatesList();
    }
}