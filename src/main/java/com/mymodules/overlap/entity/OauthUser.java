package com.mymodules.overlap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("KAKAO")  // "KAKAO" 값으로 저장
public class OauthUser extends User {

    protected OauthUser() {
        super();  // JPA 기본 생성자
    }

    public OauthUser(String name, String oauthId) {
        super(name, oauthId);  // User 생성자 호출 (OAuth는 비밀번호 필요 없음)
    }

}
