//package com.mymodules.overlap.filter;
//
//import com.mymodules.overlap.service.TurnstileService;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.annotation.Order;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@Order(Integer.MIN_VALUE)  // 필터 우선순위 최상위 설정
//public class TurnstileValidationFilter implements Filter {
//
//    private final TurnstileService turnstileService;
//
//    // 필터링을 제외할 URL 패턴 목록
//    private static final List<String> EXCLUDED_URLS = Arrays.asList(
//            "/css/", "/js/", "/images/", "/favicon.ico", "/captcha"  // 정적 파일 및 캡챠 페이지 예외처리
//    );
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//        String requestURI = httpRequest.getRequestURI();
//
//        log.info("🔥 TurnstileValidationFilter 실행됨! (doFilter 시작) - 요청 URL: {}", requestURI);
//
//        // 정적 파일 및 예외처리된 URL들은 필터링 제외
//        if (EXCLUDED_URLS.stream().anyMatch(requestURI::startsWith)) {
//            log.info("🚫 Turnstile 검증 제외된 URL: {}", requestURI);
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // Turnstile 토큰 확인
//        String turnstileToken = httpRequest.getParameter("cf-turnstile-response");
//
//        if (turnstileToken == null || turnstileToken.isEmpty()) {
//            log.warn("⚠️ Turnstile token is missing. Redirecting to /captcha");
//            httpResponse.sendRedirect("/captcha");
//            return;
//        }
//
//        // Turnstile 검증
//        boolean isValid = turnstileService.validateTurnstileToken(turnstileToken, request.getRemoteAddr());
//
//        if (!isValid) {
//            log.warn("⚠️ Turnstile validation failed. Redirecting to /captcha");
//            httpResponse.sendRedirect("/captcha");
//            return;
//        }
//
//        log.info("✅ Turnstile 검증 성공 - 요청 URL: {}", requestURI);
//        chain.doFilter(request, response);
//    }
//}