package com.mymodules.overlap.controller;


import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.service.OauthService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor

public class OauthController {

    private final OauthService oauthService;
    private final JwtUtil jwtUtil;

    @GetMapping("/oauth/kakao/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {

        String JwtToken = oauthService.createUser(code);
        jwtUtil.addJwtToCookie(JwtToken,response);

        response.sendRedirect("/test");
    }

}
