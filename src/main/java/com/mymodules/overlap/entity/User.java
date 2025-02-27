package com.mymodules.overlap.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorColumn(name = "user_type") // 구분칼럼임. 상속받는 클래스에서 DiscriminatorValue 로 TEMPORARY, KAKAO 로 구분.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // 모든 사용자 데이터를 한 테이블에 저장

public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;
    private String username;


    protected User() { }  // JPA 기본 생성자 필수라네요

    public User(String username, String password) {
        this.username = username;

    }

}
