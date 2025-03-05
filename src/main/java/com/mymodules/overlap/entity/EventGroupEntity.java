package com.mymodules.overlap.entity;

import jakarta.persistence.*;

@Entity
public class EventGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // User 엔티티와 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String start_time;

    @Column(nullable = false)
    private String end_time;

    @Column(nullable = false)
    private String select_date;

    // Schedule이 TimeTable과 1:1 관계의 주인이 되어 외래키를 관리합니다.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "timetable_id", referencedColumnName = "id", nullable = false)
    private TimeTable timeTable;

}