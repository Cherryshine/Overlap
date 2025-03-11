package com.mymodules.overlap.controller;

import com.mymodules.overlap.service.CaptchaService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @PostMapping("/verify-token")
    public ModelAndView verifyToken(@RequestBody Map<String, String> req, HttpServletResponse response) {
        String token = req.get("token");

        if (token == null || token.isEmpty()) {
            return new ModelAndView("redirect:/captcha"); // 다시 캡차 페이지로 이동
        }

        boolean isValid = captchaService.validateTurnstileToken(token, response);

        if (isValid) {
            log.info("@");
            return new ModelAndView("redirect:/");
        } else {
            log.warn("redirect failed");
            return new ModelAndView("redirect:/captcha"); // 다시 캡차 페이지로 이동
        }
    }
}