package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.service.KakaoCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class KakaoCalendarController {

    private final JwtUtil jwtUtil;
    private final KakaoCalendarService kakaoCalendarService;

    @GetMapping("/test/token")
    public String getToken(HttpServletRequest request) {
            String jwtToken = jwtUtil.getJwtFromCookies(request);
            String oauthId = jwtUtil.getSubject(jwtToken);
            System.out.println(oauthId);
            String calendar = kakaoCalendarService.getKakaoCalendar(oauthId);
            System.out.println(calendar);
            return jwtToken;
    }


}
