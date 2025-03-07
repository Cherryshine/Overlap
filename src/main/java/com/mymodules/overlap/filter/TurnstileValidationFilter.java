package com.mymodules.overlap.filter;

import com.mymodules.overlap.service.CaptchaService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE)  // 필터 우선순위 최상위 설정
public class TurnstileValidationFilter extends OncePerRequestFilter {

    private final CaptchaService captchaService;

    // 필터링을 제외할 URL 패턴 목록
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/css/", "/js/", "/images/", "/favicon.ico", "/captcha", "/captcha/verify-token", "/cdn-cgi/*","/cdn-cgi/challenge-platform/*"  // 정적 파일 및 캡챠 페이지 예외처리
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();

            log.info("🔥 TurnstileValidationFilter 실행됨! (doFilter 시작) - 요청 URL: {}", requestURI);

            // 정적 파일 및 예외처리된 URL들은 필터링 제외
            if (EXCLUDED_URLS.stream().anyMatch(requestURI::startsWith)) {
                log.info("🚫 Turnstile 검증 제외된 URL: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Turnstile 토큰 확인
            String turnstileToken = request.getParameter("cf-turnstile-response");

            if (turnstileToken == null || turnstileToken.isEmpty()) {
                log.warn("⚠️ Turnstile token is missing. Redirecting to /captcha");
                response.sendRedirect("/captcha");
                return;
            }

            // Turnstile 검증
            String remoteIp = request.getRemoteAddr();
            boolean isValid = captchaService.validateTurnstileToken(turnstileToken);

            if (!isValid) {
                log.warn("⚠️ Turnstile validation failed. Redirecting to /captcha");
                response.sendRedirect("/captcha");
                return;
            }

            log.info("✅ Turnstile 검증 성공 - 요청 URL: {}", requestURI);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"서버 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }
}