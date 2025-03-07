//package com.mymodules.overlap.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.http.MediaType;
//
//
//import java.util.Map;
//
//@Slf4j
//@Service
//public class TurnstileService {
//
//    @Value("${turnstile.secret-key}")
//    private String secretKey;
//
//    private final WebClient webClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
//
//    public TurnstileService(@Qualifier("webClientBuilderBean") WebClient.Builder webClientBuilder){
//        this.webClient = webClientBuilder.build(); // ✅ WebClient 인스턴스 생성
//        log.info("✅ WebClient가 정상적으로 주입됨: {}", webClient);
//    }
//
//    public boolean validateTurnstileToken(String token, String remoteIp) {
//        try {
//            Map<String, String> requestBody = Map.of(
//                    "secret", secretKey,
//                    "response", token,
//                    "remoteip", remoteIp
//            );
//
//            // ✅ WebClient를 사용하여 비동기 요청 수행
//            String responseJson = webClient.post()
//                    .uri(TURNSTILE_VERIFY_URL)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(String.class) // 응답을 문자열로 변환
//                    .block(); // 블로킹 방식으로 처리 (필요하면 비동기 방식으로 변경 가능)
//
//            if (responseJson == null) {
//                log.warn("Turnstile API 응답이 null입니다.");
//                return false;
//            }
//
//            JsonNode jsonResponse = objectMapper.readTree(responseJson);
//            boolean success = jsonResponse.get("success").asBoolean();
//
//            if (!success) {
//                log.warn("Turnstile validation failed: {}", jsonResponse);
//            }
//
//            return success;
//
//        } catch (Exception e) {
//            log.error("Turnstile validation error", e);
//            return false;
//        }
//    }
//}