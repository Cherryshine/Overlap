package com.mymodules.overlap.entity;

import jakarta.persistence.*;

@Entity
public class TimeTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String title_id;

    // 양방향 1:1 관계에서 TimeTable이 Schedule의 주인이 아닌 경우 mappedBy 설정
    @OneToOne(mappedBy = "timeTable")
    private EventGroup eventGroup;


}
