package com.mymodules.overlap.service;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.dto.KakaoTokenResponseDto;
import com.mymodules.overlap.dto.KakaoUserInfoDto;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Service

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


    public OauthService(UserRepository userRepository, @Qualifier("webClientBuilderBean") WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webClient = webClientBuilder.build(); // ✅ WebClientConfig에서 주입된 WebClient.Builder 사용
        log.info("✅ WebClient가 정상적으로 주입됨: {}", webClient);
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
            log.info("redirect_uri = {}", REDIRECT_URI);

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
                return "/";
            }
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + tokenResponse);

            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();

            // 엑세스토큰을 이용하여 유저 정보 가져오기
            KakaoUserInfoDto getUserInfo = getUserInfo(accessToken);

            // name 가져오기
            String name = getUserInfo.getKakaoAccount().getProfile().getNickname();
            String oauthId = getUserInfo.getId();
            String thumbnailImageUrl = getUserInfo.getKakaoAccount().getProfile().getThumbnailImageUrl();
            log.info(name);

            // db에 유저 이름 저장 추상 클래스인 User 상속받은 OauthUser 엔티티를 이용하여 저장 (type = kakao)
            User user = new User(name, oauthId, accessToken,refreshToken,thumbnailImageUrl);

            User RegisteredUser = userRepository.findByUsername(name);

            if (RegisteredUser == null) {
                userRepository.save(user);
            } else {
                System.out.println("이미 가입된 사용자 : " + RegisteredUser.getUsername());
                RegisteredUser.setAccessToken(accessToken);

            }
            return jwtUtil.createTokenWithCaptcha(oauthId,true,false);

        } catch (Exception e) {
            log.error("jwt 토큰 발급실패:{}",e.getMessage(), e);
            return "jwt 토큰 발급실패";
        }
    }

    @Transactional
    protected KakaoUserInfoDto getUserInfo (String accessToken){

        KakaoUserInfoDto userInfo = webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoDto.class)
                .block();

/*
    카카오 로그인 Raw Json
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
        System.out.println(name + profileImage);
        return userInfo;
    }



    public String validateAccessToken(String oauthId) {
        // DB에서 해당 사용자 정보 조회 (저장된 엑세스 토큰 포함)
        User user = userRepository.findByUuid(oauthId);
        if (user == null) {
            log.warn("사용자 {}를 찾을 수 없습니다.", oauthId);
            return "사용자 없음";
        }

        String accessToken = user.getAccessToken();
        try {
            String tokenInfoUrl = "https://kapi.kakao.com/v1/user/access_token_info";

            // 저장된 토큰을 이용하여 카카오 API에 GET 요청을 보냄
            KakaoTokenResponseDto tokenInfo = webClient.get()
                    .uri(tokenInfoUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponseDto.class)
                    .block();

            log.info("유효한 Access Token입니다. tokenInfo: {}", tokenInfo);
            return "유효";
        } catch (Exception e) {
            log.warn("만료되었거나 잘못된 Access Token입니다. 에러: {}", e.getMessage());
            return "만료";
        }
    }

    @Transactional
    public String newAccessToken(String oauthId){

        User user = userRepository.findByUuid(oauthId);

        if (user == null) {
            log.warn("사용자 {}를 찾을 수 없습니다.", oauthId);
            return "사용자 없음";
        }
        String refreshToken = user.getRefreshToken();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", CLIENT_ID);
        params.add("refresh_token", refreshToken);
        params.add("client_secret", CLIENT_SECRET);

        KakaoTokenResponseDto refreshResponse = webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        if (refreshResponse != null) {
            String newToken = refreshResponse.getAccessToken();
            user.setAccessToken(newToken);
            System.out.println(newToken);
            userRepository.save(user);
        }

        return "엑세스토큰 발급";
    }

    @Transactional
    public void removeToken(String token) {
        if (token == null) {
            log.warn("토큰이 null입니다. 로그아웃 처리를 진행할 수 없습니다.");
            return;
        }
        
        // JWT 토큰에서 oauthId 추출
        String oauthId = jwtUtil.getSubject(token);
        if (oauthId == null) {
            log.warn("유효하지 않은 토큰입니다. oauthId를 추출할 수 없습니다.");
            return;
        }
        
        // oauthId로 사용자 찾기
        User user = userRepository.findByUuid(oauthId);
        if (user == null) {
            log.warn("사용자 {}를 찾을 수 없습니다.", oauthId);
            return;
        }
        
        // 사용자의 토큰 정보 삭제
        user.setAccessToken(null);
        user.setRefreshToken(null);
        userRepository.save(user);
        
        log.info("사용자 {} 토큰이 성공적으로 삭제되었습니다.", oauthId);
    }

}