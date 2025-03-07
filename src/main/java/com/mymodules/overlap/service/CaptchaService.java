package com.mymodules.overlap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class CaptchaService {

    @Value("${turnstile.secret-key}")
    private String secretKey;
    @Value(("${turnstile.site-key}"))
    private String siteKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public CaptchaService(@Qualifier("webClientBuilderBean") WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build(); // ✅ WebClient 인스턴스 생성
        log.info("✅ WebClient가 정상적으로 주입됨: {}", webClient);
    }

    public boolean validateTurnstileToken(String token) {
        if (secretKey == null || token == null) {
            System.out.println("@#@$@#$@#$" + token);
            log.error("secretKey 또는 token이 null입니다.");
            return false;
        }
        System.out.println("@#@$@#$@#$" + token);

        try {
            Map<String, String> requestBody = Map.of(
                    "secret", secretKey,
                    "response", token
            );

            // WebClient를 사용하여 비동기 요청 수행
            String responseJson = webClient.post()
                    .uri(TURNSTILE_VERIFY_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Cloudflare Turnstile API 호출 실패: HTTP 상태 코드 - {}", response.statusCode());
                                return Mono.error(new RuntimeException("Cloudflare Turnstile API 호출 실패"));
                            })
                    .bodyToMono(String.class) // 응답을 문자열로 변환
                    .block(); // 동기 처리

            if (responseJson == null) {
                log.warn("Turnstile API 응답이 null입니다.");
                return false;
            }
            System.out.println(responseJson);
            JsonNode jsonResponse = objectMapper.readTree(responseJson);
            boolean success = jsonResponse.get("success").asBoolean();

            if (!success) {
                log.warn("Turnstile 검증 실패: 응답 내용 - {}", jsonResponse);
            }

            return success;

        } catch (Exception e) {
            log.error("Turnstile 검증 중 오류 발생", e);
            return false;
        }
    }
}

