package com.mymodules.overlap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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
        User user = userRepository.findByUuid(oauthId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        String accessToken = user.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("엑세스 토큰이 존재하지 않습니다.");
        }

        System.out.println("엑세스 토큰: " + accessToken);

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode timeNode = objectMapper.createObjectNode();
        timeNode.put("start_at", "2025-03-06T03:00:00Z");
        timeNode.put("end_at", "2025-03-07T06:00:00Z");
        timeNode.put("time_zone", "Asia/Seoul");
        timeNode.put("all_day", false);
        timeNode.put("lunar", false);

        ObjectNode locationNode = objectMapper.createObjectNode();
        locationNode.put("name", "카카오");
        locationNode.put("location_id", 18577297);
        locationNode.put("address", "경기 성남시 분당구 판교역로 166");
        locationNode.put("latitude", 37.39570088983171);
        locationNode.put("longitude", 127.1104335101161);

        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("title", "OverLap 에서 생성한 일정입니다.");
        eventNode.set("time", timeNode);
        eventNode.put("rrule", "FREQ=DAILY;UNTIL=20250307T000000Z");
        eventNode.put("description", "일정 설명");
        eventNode.set("location", locationNode);
        eventNode.putArray("reminders").add(2145);
        eventNode.put("color", "RED");

        String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(eventNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 중 오류 발생", e);
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("calendar_id", "primary");
        formData.add("event", eventJson);
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

//
//        String eventList = webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("https")
//                        .host("kapi.kakao.com")
//                        .path("/v2/api/calendar/events")
//                        .queryParam("calendar_id", "primary")
//                        .build())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        System.out.println("일정목록 ::::: "+eventList);
//
//
//        String eventEdited = webClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("https")
//                        .host("kapi.kakao.com")
//                        .path("/v2/api/calendar/update/event/host")
//                        .queryParam("event_id", "67c83a66031af43f915e330d_20250306T030000Z")
//                        .build())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();

        return response;
    }






}