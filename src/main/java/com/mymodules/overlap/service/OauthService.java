package com.mymodules.overlap.service;

import com.mymodules.overlap.config.JwtUtil;

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
//@RequiredArgsConstructor
public class OauthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
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

    // ✅ WebClient.Builder를 생성자로 주입받아 WebClient 인스턴스 생성
    public OauthService(UserRepository userRepository, WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webClient = webClientBuilder.build(); // WebClient 인스턴스 생성
    }


    @Transactional
    public String createUser (String code){
            try {

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
                String oauthId = (String) getUserInfo.get("oauthId");
                log.info(name);

                // db에 유저 이름 저장 추상 클래스인 User 상속받은 OauthUser 엔티티를 이용하여 저장 (type = kakao)
                User user = new OauthUser(name, oauthId);

                User RegisteredUser = userRepository.findByusername(name);

                if (RegisteredUser == null) {
                    userRepository.save(user);
                } else {
                    System.out.println("이미 가입" + RegisteredUser);
                }


                return jwtUtil.createToken(name);

                } catch (Exception e) {
                    log.error("jwt 토큰 발급실패:{}",e.getMessage(), e);
                    return "jwt 토큰 발급실패";
                }
    }

    @Transactional
    protected Map<String,Object> getUserInfo (String accessToken){

        KakaoUserInfoDto userInfo = webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoDto.class)
                .block();

/*      카카오 로그인 Raw Json
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(userInfo);
            System.out.println(jsonString);  // ✅ JSON 형식으로 DTO 출력
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        if (userInfo == null || userInfo.getId() == null) {
            System.out.println("사용자 정보를 가져오지 못했습니다.");
            return null;
        }
        // 유저 프로필의 이름 가져오기 (유저가 설정한 닉네임)
        String oauthId = userInfo.getId();
        System.out.println(oauthId+"@@@@@@@@@@@@@@@@@@@");
        String name = userInfo.getKakaoAccount().getProfile().getNickname();
        String profileImage = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();
        return Map.of("name", name, "profileImage", profileImage, "oauthId", oauthId);
    }


}