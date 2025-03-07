package com.mymodules.overlap.entity;

import com.mymodules.overlap.dto.EventGroupRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class EventGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // User 엔티티와 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "startTime", nullable = false)
    private String startTime;

    @Column(name = "endTime", nullable = false)
    private String endTime;

    @Column(name = "selectdates", nullable = false, columnDefinition = "TEXT")
    private String selectDates;

    // Schedule이 TimeTable과 1:1 관계의 주인이 되어 외래키를 관리합니다.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = true)
    private TimeTable timeTable;

    // List로 가져오기 위한 헬퍼 메서드
    public List<String> getSelectDatesList() {
        if (selectDates == null || selectDates.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(selectDates.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    // List로 설정하기 위한 헬퍼 메서드
    public void setSelectDatesList(List<String> dates) {
        if (dates == null || dates.isEmpty()) {
            this.selectDates = "";
        } else {
            this.selectDates = String.join(",", dates);
        }
    }

    public void updateEvent(String title, String startTime, String endTime, String newDates) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.selectDates = newDates;
    }

    // ✅ 새로운 이벤트를 위한 생성자 추가
    public EventGroupEntity(User user, String title, String startTime, String endTime, String selectDates) {
        this.user = user;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.selectDates = selectDates;
    }


}