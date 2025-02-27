package com.mymodules.overlap.controller;


import com.mymodules.overlap.service.OauthService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor

public class OauthController {

    private final OauthService oauthService;

    @GetMapping("/oauth/kakao/callback")
    public String kakaoCallback(@RequestParam String code, HttpServletResponse response) {

        oauthService.createUser(code);

        return "test";
    }
}
