package com.mymodules.overlap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfoDto {

    private String id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    public static class KakaoAccount {
        private String email;

        @JsonProperty("phone_number")
        private String phoneNumber;

        private Profile profile;
    }

    @Getter
    @Setter
    public static class Profile {
        private String nickname;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;
    }
}