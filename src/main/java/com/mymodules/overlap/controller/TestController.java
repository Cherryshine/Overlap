package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class TestController {

    private final JwtUtil jwtUtil;

    @GetMapping("/oauth/logout-success")
    public String logout(HttpServletResponse response){

        jwtUtil.removeCookie(response);

        return "/logout-success";
    }


}
