package com.mymodules.overlap.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EventGroupResponseDto {
    // 250318 오전 10시 임시 주석처리. URL 리스폰스 클라이언트로 보내고 여기로 리다이렉트 해야함.
//    private String url;
    private String username;
    private String title;
    private List<LocalDate> dates;
    private LocalTime startTime;
    private LocalTime endTime;
    //    private LocalDate createAt;
//    private LocalDate expiredAt;
    private boolean creatorParticipates;
    private List<SelectedTime> creatorSelectedTimes;
    private String url;

    public EventGroupResponseDto(String username, EventGroupRequestDto req) {
        this.username = username;
        title = req.getTitle();
        dates = req.getDates();
        startTime = req.getStartTime();
        endTime = req.getEndTime();
        creatorParticipates = req.isCreatorParticipates();
        creatorSelectedTimes = req.getCreatorSelectedTimes();
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
