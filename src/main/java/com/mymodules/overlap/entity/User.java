package com.mymodules.overlap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorColumn(name = "user_type") // 구분 칼럼 (상속받는 클래스에서 DiscriminatorValue로 구분)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // 모든 사용자 데이터를 한 테이블에 저장
@Getter
@Setter
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    private String oauthId;

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private String thumbnailImageUrl;

    // User 하나가 여러 Schedule을 가질 수 있음 (1:N 관계)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventGroupEntity> eventGroupEntities = new ArrayList<>();

    protected User() { }  // JPA 기본 생성자

    public User(String username, String oauthId, String accessToken, String refreshToken, String thumbnailImageUrl) {
        this.username = username;
        this.oauthId = oauthId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
}