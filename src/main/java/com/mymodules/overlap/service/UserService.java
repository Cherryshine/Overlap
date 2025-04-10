package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.KakaoUserInfoDto;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OauthService oauthService;


    public Map<String,String> getProfile(String oauthId) {

        User user = userRepository.findByUuid(oauthId);
        
        // 사용자가 존재하지 않는 경우 게스트 응답 반환
        if (user == null) {
            log.warn("사용자 정보를 찾을 수 없음: {}", oauthId);
            Map<String, String> guestResponse = new HashMap<>();
            guestResponse.put("usertype", "guest");
            return guestResponse;
        }

//         KakaoUserInfoDto 에서 가져오기
        KakaoUserInfoDto userInfo = oauthService.getUserInfo(user.getAccessToken());
        String thumbnailUrl = userInfo.getKakaoAccount().getProfile().getThumbnailImageUrl();

//        String thumbnailUrl = user.getThumbnailImageUrl();
        System.out.println(thumbnailUrl);

        Map<String,String> map = new HashMap<>();

        map.put("thumbnailImageUrl",thumbnailUrl);

        return map;
    }

    public Map<String,String> getUserName(String oauthId) {

        User user = userRepository.findByUuid(oauthId);
        
        // 사용자가 존재하지 않는 경우 게스트 응답 반환
        if (user == null) {
            log.warn("사용자 정보를 찾을 수 없음: {}", oauthId);
            Map<String, String> guestResponse = new HashMap<>();
            guestResponse.put("usertype", "guest");
            return guestResponse;
        }

        Map<String,String> map = new HashMap<>();

        map.put("username",user.getUsername());

        return map;
    }




}