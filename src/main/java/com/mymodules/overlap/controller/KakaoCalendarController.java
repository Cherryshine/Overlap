package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.repository.UserRepository;
import com.mymodules.overlap.service.KakaoCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class KakaoCalendarController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final KakaoCalendarService kakaoCalendarService;

//    @GetMapping("/test/getCalendar")
//    public String getGetKakaoCalendar() {
//
//
//        return ResponseEntity.ok(responseDto);
//    }


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
