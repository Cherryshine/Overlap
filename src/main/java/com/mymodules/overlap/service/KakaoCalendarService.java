package com.mymodules.overlap.service;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.entity.OauthUser;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service

public class KakaoCalendarService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;

    public KakaoCalendarService(UserRepository userRepository, @Qualifier("webClientBuilderBean") WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webClient = webClientBuilder.build(); // ✅ WebClientConfig에서 주입된 WebClient.Builder 사용
        log.info("✅ WebClient가 정상적으로 주입됨: {}", webClient);
    }

//    public String getKakaoCalendar(String jwtToken){
//        jwtToken = jwtUtil.substringToken(jwtToken);
//        String oauthId = jwtUtil.getSubject(jwtToken);
//        User user = userRepository.findByOauthId(oauthId);
//        System.out.println(user+"유저임");
//        String accessToken = user.getAccessToken();
//        System.out.println(accessToken);
//
//        return null;
//    }


    public String getKakaoCalendar(String oauthId) {
        System.out.println("OAuth ID: " + oauthId);

        // 사용자 정보 조회
        User user = userRepository.findByOauthId(oauthId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        String accessToken = user.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("엑세스 토큰이 존재하지 않습니다.");
        }

        System.out.println("엑세스 토큰: " + accessToken);

        // 쿼리 파라미터 생성
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("calendar_id", "primary");
        formData.add("event", "{\"title\": \"OverLap 에서 생성한 일정입니다.\"," +
                "\"time\": {" +
                "\"start_at\": \"2025-03-06T03:00:00Z\"," +
                "\"end_at\": \"2025-03-07T06:00:00Z\"," +
                "\"time_zone\": \"Asia/Seoul\"," +
                "\"all_day\": false," +
                "\"lunar\": false" +
                "}," +
                "\"rrule\":\"FREQ=DAILY;UNTIL=20250307T000000Z\"," +
                "\"description\": \"일정 설명\"," +
                "\"location\": {" +
                "\"name\": \"카카오\"," +
                "\"location_id\": 18577297," +
                "\"address\": \"경기 성남시 분당구 판교역로 166\"," +
                "\"latitude\": 37.39570088983171," +
                "\"longitude\": 127.1104335101161" +
                "}," +
                "\"reminders\": [2145]," +
                "\"color\": \"RED\"}");

        // API 요청
        String response = webClient.post()
                .uri("https://kapi.kakao.com/v2/api/calendar/create/event")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("카카오 캘린더 생성 응답: " + response);

        return response;
    }






}
