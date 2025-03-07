package com.mymodules.overlap.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class EventGroupRequestDto {

    private String title;
    private String startTime;
    private String endTime;
    private List<Map<String, Integer>> selectedDates;


}
