package com.mymodules.overlap.repository;

import com.mymodules.overlap.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRepository {
    public final UserJpaRepository userJpaRepository;

    public User findByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    public User findByOauthId(String oauthId) {
        return userJpaRepository.findByOauthId(oauthId);
    }

    public void save(User user) {
        userJpaRepository.save(user);
    }
}
