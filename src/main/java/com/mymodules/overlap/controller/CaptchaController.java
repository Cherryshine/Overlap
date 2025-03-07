package com.mymodules.overlap.controller;

import com.mymodules.overlap.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<String> verifyToken(@RequestBody Map<String, String> req) {
        String token = req.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("토큰이 없습니다.");
        }
        System.out.println("나 컨트롤러 들어왔어");

        // 파라미터에서 Turnstile 토큰 가져오기
//        String token = request.getParameter("cf-turnstile-response");

        // 토큰 유효성 검사
        boolean isValid = captchaService.validateTurnstileToken(token);
        System.out.println(isValid);

        if (isValid) {
            System.out.println(isValid + " : 유효한 토큰입니다.");
            return ResponseEntity.ok("토큰이 유효합니다.");
        } else {
            System.out.println("유효하지 않은 토큰");
            return ResponseEntity.status(400).body("유효하지 않은 토큰입니다.");
        }
    }
}
