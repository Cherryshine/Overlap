package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.KakaoTokenResponseDto;
import com.mymodules.overlap.dto.KakaoUserInfoDto;
import com.mymodules.overlap.entity.OauthUser;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service

public class OauthService {

    private final UserRepository userRepository;
    private final WebClient webClient;


    @Value("${kakao.client.id}")
    private String CLIENT_ID;

    @Value("${kakao.redirect.uri}")
    private String REDIRECT_URI;

    @Value("${kakao.token.url}")
    private String TOKEN_URL;

    @Value("${kakao.user.info.url}")
    private String USER_INFO_URL;

    @Value("${kakao.client.secret}")
    private String CLIENT_SECRET;

    // WebClient.Builder를 주입받아 WebClient 인스턴스 생성
    public OauthService(UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.webClient = webClientBuilder.baseUrl("https://kauth.kakao.com").build();
    }

    @Transactional
    public String createUser (String code){

            // 1. 요청 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", CLIENT_ID);
            params.add("redirect_uri", REDIRECT_URI);
            params.add("client_secret", CLIENT_SECRET);
            params.add("grant_type", "authorization_code");

            // 2. Kakao API에 액세스 토큰 요청
            KakaoTokenResponseDto tokenResponse = webClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponseDto.class)
                    .block();
            // 토큰 못가져왔을때
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                System.out.println("카카오 API 응답이 없습니다.");
                return "test";
            }

            String accessToken = tokenResponse.getAccessToken();

            // 엑세스토큰을 이용하여 유저 정보 가져오기
            Map<String, Object> getUserInfo = getUserInfo(accessToken);

            // name 가져오기
            String name = (String) getUserInfo.get("name");

            log.info(name);

            User user = new OauthUser(name);
            userRepository.save(user);

            return "success";
    }

    @Transactional
    protected Map<String,Object> getUserInfo (String accessToken){

        KakaoUserInfoDto userInfo = webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoDto.class)
                .block();

        if (userInfo == null || userInfo.getId() == null) {
            System.out.println("사용자 정보를 가져오지 못했습니다.");
            return null;
        }
        // 유저 프로필의 이름 가져오기 (유저가 설정한 닉네임)
        String name = userInfo.getKakaoAccount().getProfile().getNickname();
        String profileImage = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();

        return Map.of("name", name, "profileImage", profileImage);
    }


}