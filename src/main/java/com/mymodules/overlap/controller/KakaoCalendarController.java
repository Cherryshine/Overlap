package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.repository.UserRepository;
import com.mymodules.overlap.service.KakaoCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

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
    public String getToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
            return authorizationHeader.substring(8);
        }
        return "토큰이 없습니다.";
    }


}
