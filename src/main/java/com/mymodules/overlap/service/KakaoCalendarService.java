package com.mymodules.overlap.service;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.entity.OauthUser;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoCalendarService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    public String getKakaoCalendar(String jwtToken){
        jwtToken = jwtUtil.substringToken(jwtToken);
        String oauthId = jwtUtil.getSubject(jwtToken);
        User user = userRepository.findByOauthId(oauthId);
        System.out.println(user+"유저임");
        String accessToken = user.getAccessToken();
        System.out.println(accessToken);

        return null;
    }



}
