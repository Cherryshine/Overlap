package com.mymodules.overlap.controller;


import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.service.OauthService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OauthController {

    private final OauthService oauthService;
    private final JwtUtil jwtUtil;

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {

        String JwtToken = oauthService.createUser(code);
        jwtUtil.addJwtToCookie(JwtToken,response);

        response.sendRedirect("/");
    }

    @GetMapping("/authorize")
    public void kakaoAuthCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect("/test");
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(HttpServletRequest request) {
        // 쿠키에서 JWT 추출 (예시 메서드)
        String token = jwtUtil.getJwtFromCookies(request);
        if (token == null) {
            return ResponseEntity.badRequest().body("쿠키에 토큰이 없습니다.");
        }

        // JWT 파싱해서 사용자 식별자(oauthId) 추출
        String oauthId = jwtUtil.getSubject(token);
        if (oauthId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // 토큰 검증 로직
        String result = oauthService.validateAccessToken(oauthId);
        if ("유효".equals(result)) {
            return ResponseEntity.ok("Access Token이 유효합니다.");
        } else if ("사용자 없음".equals(result)) {
            return ResponseEntity.badRequest().body("사용자를 찾을 수 없습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Access Token이 만료되었거나 올바르지 않습니다.");
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken(HttpServletRequest request) {
        String token = jwtUtil.getJwtFromCookies(request);

        if (token == null) {
            return ResponseEntity.badRequest().body("쿠키에 토큰이 없습니다.");
        }

        // JWT 파싱해서 사용자 식별자(oauthId) 추출
        String oauthId = jwtUtil.getSubject(token);
        if (oauthId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
        oauthService.newAccessToken(oauthId);
        return ResponseEntity.ok("success");
    }



}
