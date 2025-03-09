package com.mymodules.overlap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymodules.overlap.config.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
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

    private final JwtUtil jwtUtil;

    @Value("${turnstile.secret-key}")
    private String secretKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public CaptchaService(@Qualifier("webClientBuilderBean") WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        this.webClient = webClientBuilder.build();
        this.jwtUtil = jwtUtil;
    }

    /**
     * ✅ Turnstile 검증 수행 후 JWT 발급 (쿠키에 저장)
     */
    public boolean validateTurnstileToken(String token, HttpServletResponse response) {
        if (secretKey == null || token == null) {
            log.error("🚨 secretKey 또는 token이 null입니다.");
            return false;
        }

        try {
            Map<String, String> requestBody = Map.of(
                    "secret", secretKey,
                    "response", token
            );

            String responseJson = webClient.post()
                    .uri(TURNSTILE_VERIFY_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            res -> {
                                log.error("🚨 Cloudflare Turnstile API 호출 실패: HTTP 상태 코드 - {}", res.statusCode());
                                return Mono.error(new RuntimeException("Cloudflare Turnstile API 호출 실패"));
                            })
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null) {
                log.warn("🚨 Turnstile API 응답이 null입니다.");
                return false;
            }

            JsonNode jsonResponse = objectMapper.readTree(responseJson);
            boolean success = jsonResponse.get("success").asBoolean();

            if (success) {
                log.info("✅ Turnstile 검증 성공. JWT 발급 중...");
                String captchaToken = jwtUtil.createTokenWithCaptcha("guest",true,true);
                jwtUtil.addJwtToCookie(captchaToken, response); // ✅ HTTP 응답에 쿠키 추가
                log.info("🍪 JWT가 쿠키에 정상적으로 저장됨.");
            } else {
                log.warn("⚠️ Turnstile 검증 실패: {}", jsonResponse);
            }

            return success;
        } catch (Exception e) {
            log.error("🚨 Turnstile 검증 중 오류 발생", e);
            return false;
        }
    }


}