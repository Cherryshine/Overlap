package com.mymodules.overlap.repository;


import com.mymodules.overlap.entity.EventGroup;
import com.mymodules.overlap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventGroup, Long> {




    Optional<EventGroup> findFirstByUserOrderByIdDesc(User user);
}
