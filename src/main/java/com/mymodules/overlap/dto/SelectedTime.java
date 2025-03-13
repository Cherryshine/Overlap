package com.mymodules.overlap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class SelectedTime {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
