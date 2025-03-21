package com.mymodules.overlap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mymodules.overlap.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String name);
    User findByUuid(String uuid);
    User findByAccessToken(String accessToken);
    User findBythumbnailImageUrl(String thumbnailImageUrl);
}