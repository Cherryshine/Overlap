package com.mymodules.overlap.filter;

import com.mymodules.overlap.config.JwtUtil;
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
    private final JwtUtil jwtUtil;

    // 필터링을 제외할 URL 패턴 목록
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/css/", "/js/", "/images/", "/favicon.ico", "/captcha",
            "/captcha/verify-token", "/cdn-cgi/*", "/cdn-cgi/challenge-platform/*",
            "/oauth/kakao/callback", "/api/schedules"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            log.info("🔥 TurnstileValidationFilter 실행됨! (doFilter 시작) - 요청 URL: {}", requestURI);

            // 제외 대상 URL은 바로 필터 통과
            if (isExcludedUrl(requestURI)) {
                log.info("🚫 Turnstile 검증 제외된 URL: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 1. JWT 쿠키에서 토큰 가져오기
            String captchaToken = jwtUtil.getJwtFromCookies(request);

            if (captchaToken != null) {
                log.info("✅ JWT 쿠키 감지됨, 검증 시작...");

                // 2. JWT 토큰 유효성 검증
                boolean valid = jwtUtil.validateToken(captchaToken);

                if (valid) {
                    log.info("✅ JWT 유효성 검증 통과! 요청 계속 진행.");
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    log.warn("❌ JWT 만료됨. 쿠키 삭제 및 캡챠 페이지로 리다이렉트.");
                    jwtUtil.removeCookie(response);

                    // ✅ 응답 헤더 Set-Cookie 로그 확인
                    log.info("✅ 삭제 후 헤더 상태: {}", response.getHeaders("Set-Cookie"));

                    response.sendRedirect("/captcha");
                    return;
                }
            }

            log.info("❌ JWT 없음. Turnstile 토큰 검증 시작...");

            // 3. Turnstile 토큰 파라미터 가져오기
            String turnstileToken = request.getParameter("cf-turnstile-response");

            if (turnstileToken == null || turnstileToken.isEmpty()) {
                log.warn("⚠️ Turnstile token 파라미터 누락. 캡챠 페이지로 이동.");
                response.sendRedirect("/captcha");
                return;
            }

            // 4. Turnstile 검증 로직 수행
            boolean isValidCaptcha = captchaService.validateTurnstileToken(turnstileToken, response);

            if (!isValidCaptcha) {
                log.warn("⚠️ Turnstile 검증 실패. 쿠키 삭제 및 캡챠 페이지로 이동.");

                // 이 경우 쿠키는 아마 존재하지 않겠지만 혹시 몰라 삭제
                jwtUtil.removeCookie(response);

                response.sendRedirect("/captcha");
                return;
            }

            // 5. Turnstile 검증 성공 시, JWT 발급은 captchaService에서 처리 완료
            log.info("✅ Turnstile 검증 성공. 루트 페이지로 이동!");
            response.sendRedirect("/");

        } catch (Exception e) {
            log.error("🚨 필터 처리 중 예외 발생!", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"서버 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 필터 제외 URL 검사 메서드
     */
    private boolean isExcludedUrl(String uri) {
        return EXCLUDED_URLS.stream().anyMatch(uri::startsWith);
    }
}