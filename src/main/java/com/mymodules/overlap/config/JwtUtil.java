package com.mymodules.overlap.config;

import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Log4j2
@Component
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    public static final String BEARER_PREFIX = "Bearer ";
    private final long TOKEN_TIME = 60 * 60 * 1000L;
//    private final long TOKEN_TIME = 60 * 1000L; // 10초

    @Value("${jwt.secret.key}")
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    @PostConstruct
    public void init() {
        System.out.println("🔍 JwtUtil init() 실행됨");
        System.out.println("🔍 secretKey 값: " + secretKey);

        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("🚨 jwt.secret.key 값이 설정되지 않았습니다. application.properties를 확인하세요.");
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(secretKey);
            System.out.println("🔍 Base64 디코딩 완료: " + bytes.length + " bytes");

            if (bytes.length < 32) {
                throw new IllegalArgumentException("The decoded key must be at least 256 bits (32 bytes).");
            }
            key = Keys.hmacShaKeyFor(bytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64-encoded secret key or key length is insufficient.", e);
        }
    }

//    public String createToken(String oauthId){
//
//        System.out.println("🔍 createToken() 호출됨");
//
//        if (key == null) {
//            throw new IllegalStateException("🚨 JWT Key가 초기화되지 않았습니다. @PostConstruct init()을 확인하세요.");
//        }
//
//        Date date = new Date();
//        return BEARER_PREFIX +
//                Jwts.builder()
//                        .setSubject(oauthId)
//                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
//                        .setIssuedAt(date)
//                        .signWith(key, signatureAlgorithm)
//                        .compact();
//    }

    public String createTokenWithCaptcha(String oauthId, boolean captchaSuccess, boolean isGuest) {
        log.info("🔍 createTokenWithCaptcha() 호출됨 - 사용자: {}, 캡챠 인증 여부: {}, 게스트 여부: {}",
                oauthId, captchaSuccess, isGuest);

        if (key == null) {
            throw new IllegalStateException("🚨 JWT Key가 초기화되지 않았습니다. @PostConstruct init()을 확인하세요.");
        }

        Date date = new Date();

        // ✅ subject 값 결정 (카카오 로그인 안 한 경우 "guest", 로그인한 경우 oauthId)
        String subject = isGuest ? "guest" : oauthId;

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(subject)
                        .claim("captchaVerified", captchaSuccess) // ✅ 캡챠 인증 상태 추가
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }
        // 캡챠 인증 여부 + guest 와 kakao 회원 구분


    public void addJwtToCookie(String token, HttpServletResponse res) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20");

            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
            cookie.setPath("/");
            cookie.setSecure(false);
            cookie.setHttpOnly(false);
            cookie.setMaxAge(60 * 60 * 24);

            res.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            logger.error("쿠키 인코딩 오류: " + e.getMessage());
        }
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith("Bearer%20")) {
            // "Bearer " 이후의 토큰을 잘라내되, 공백도 처리
            return tokenValue.substring("Bearer%20".length()).trim();
        }
        logger.error("Not Found Token");
        return null; // Token이 잘못되었을 경우 null을 반환
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("JWT 토큰 검증 오류: {}", e.getMessage());
        }
        return false;
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    String tokenValue = cookie.getValue();
                    if (tokenValue != null) {
                        return substringToken(tokenValue);
                    }
                }
            }
        }
        return null; // JWT 토큰이 없으면 null 반환
    }

    public String getSubject(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT 토큰에서 Subject 추출 실패: {}", e.getMessage());
            return null; // 토큰 파싱 실패 시 null 반환
        }
    }


}
