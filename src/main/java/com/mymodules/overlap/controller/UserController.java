package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/get_user/profile")
    public Map<String,String> getUserProfile(HttpServletRequest request){
        String jwtToken;
        String oauthId;
        if("guest".equals(jwtUtil.getJwtFromCookies(request))){
            Map<String, String> response = new HashMap<>();
            response.put("usertype", "guest");
            return response;
        }
        else{
            jwtToken = jwtUtil.getJwtFromCookies(request);
            oauthId = jwtUtil.getSubject(jwtToken);
            return userService.getProfile(oauthId);
        }
    }

    @GetMapping("/get_user/name")
    public Map<String,String> getUserName(HttpServletRequest request){
        String jwtToken = jwtUtil.getJwtFromCookies(request);
        String oauthId = jwtUtil.getSubject(jwtToken);

        return userService.getUserName(oauthId);
    }



}
