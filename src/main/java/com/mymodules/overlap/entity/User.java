package com.mymodules.overlap.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@DiscriminatorColumn(name = "user_type") // 구분칼럼임. 상속받는 클래스에서 DiscriminatorValue 로 TEMPORARY, KAKAO 로 구분.
public abstract class User {
    @Id
    private Long id;
    private String username;
    private String password;
}
