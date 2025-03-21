package com.mymodules.overlap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // id 자동 증가
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = true)
    private String tempUsername;

    @Column
    private String uuid;  // uuid 필드

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private String thumbnailImageUrl;

    @Column
    private String userType;  // "KAKAO" 또는 "GUEST"를 직접 저장

    @Column
    private String createdAt;

    @Column // Guest Type 일 경우, 이 필드에 맞춰 삭제됨.
    private String expireAt;

    // User 하나가 여러 Schedule을 가질 수 있음 (1:N 관계)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventGroup> eventGroupEntities = new ArrayList<>();

    // Constructors

    protected User() { }

    // Kakao User
    public User(String name, String uuid, String accessToken, String refreshToken, String thumbnailImageUrl) {
        this.username = name;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.userType = "KAKAO";
        this.createdAt = LocalDateTime.now().toString();
    }

    // Guest User
    public User(String username) {
        this.username = username;
        this.userType = "GUEST";
        this.createdAt = LocalDateTime.now().toString();
        this.expireAt = LocalDateTime.now().plusDays(30).toString();
    }

    @PostPersist
    public void onPostPersist() {
        if ("GUEST".equals(this.userType)) {
            // id 값이 DB에 저장된 후 자동으로 UUID를 설정
            this.uuid = "guest" + this.id;
        }
    }
}
