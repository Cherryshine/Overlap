package com.mymodules.overlap.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("KAKAO")  // "KAKAO" 값으로 저장
public class OauthUser extends User {

    protected OauthUser() {
        super();  // JPA 기본 생성자
    }

    public OauthUser(String name, String oauthId, String accessToken, String refreshToken, String thumbnailImageUrl) {
        super(name, oauthId, accessToken,refreshToken,thumbnailImageUrl);  // User 생성자 호출 (OAuth는 비밀번호 필요 없음)
    }

}
