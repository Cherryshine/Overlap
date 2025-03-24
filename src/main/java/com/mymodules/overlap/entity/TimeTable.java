package com.mymodules.overlap.entity;

import com.mymodules.overlap.dto.SelectedTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TimeTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 양방향 1:1 관계에서 TimeTable이 Schedule의 주인이 아닌 경우 mappedBy 설정
    @OneToOne(mappedBy = "timeTable")
    private EventGroup eventGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    private User user;
    
    // 날짜와 선택된 시간 정보를 JSON 형태로 저장
    @Column(nullable = true, columnDefinition = "TEXT")
    private String selectedTimeData;
    
    // 생성자 추가
    public TimeTable(User user, String selectedTimeData) {
        this.user = user;
        this.selectedTimeData = selectedTimeData;
    }
}
