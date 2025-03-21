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

//         KakaoUserInfoDto 에서 가져오기
          KakaoUserInfoDto userInfo = oauthService.getUserInfo(user.getAccessToken());
                String thumbnailUrl = userInfo.getKakaoAccount().getProfile().getThumbnailImageUrl();

//        String thumbnailUrl = user.getThumbnailImageUrl();
        System.out.println(thumbnailUrl);

        Map<String,String> map = new HashMap<>();

        map.put("thumbnailImageUrl",thumbnailUrl);

        return map;
    }
}
