package com.mymodules.overlap.entity;

import com.mymodules.overlap.dto.EventGroupResponseDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class EventGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // User 엔티티와 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    private User user;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "startTime", nullable = false)
    private String startTime;

    @Column(name = "endTime", nullable = false)
    private String endTime;

    @Column(name = "selectdates", nullable = false, columnDefinition = "TEXT")
    private String selectDates;

    @Column(name = "created_At", nullable = false)
    private LocalDate createdAt;

    @Column(name = "expired_At", nullable = false)
    private LocalDate expiredAt;
//
//    @Column(name = "url", nullable = false)
//    private String url;

    // Schedule이 TimeTable과 1:1 관계의 주인이 되어 외래키를 관리합니다.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = true)
    private TimeTable timeTable;


    public EventGroup(EventGroupResponseDto res, User user) {
        this.user = user;
        this.title = res.getTitle();
        this.startTime = res.getStartTime().toString(); // LocalTime을 String으로 변환
        this.endTime = res.getEndTime().toString();     // LocalTime을 String으로 변환
        this.selectDates = convertDatesToString(res.getDates());
    }

    private String convertDatesToString(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return "";
        }
        return dates.stream()
                .map(LocalDate::toString) // ISO-8601 포맷: 2024-03-14
                .collect(Collectors.joining(","));
    }

    @PrePersist
    public void onPrePersist() {
        // 엔티티가 처음 저장되기 직전에 호출됨!
        this.createdAt = LocalDate.now(); // 현재 날짜로 생성일자 셋팅
        this.expiredAt = this.createdAt.plusDays(30); // 생성일 기준 30일 뒤 만료일자 셋팅
    }
}