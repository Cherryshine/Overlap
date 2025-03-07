package com.mymodules.overlap.repository;


import com.mymodules.overlap.entity.EventGroupEntity;
import com.mymodules.overlap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventGroupEntity, Long> {




    Optional<EventGroupEntity> findFirstByUserOrderByIdDesc(User user);
}
