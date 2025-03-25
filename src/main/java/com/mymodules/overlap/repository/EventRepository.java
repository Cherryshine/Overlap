package com.mymodules.overlap.repository;


import com.mymodules.overlap.entity.EventGroup;
import com.mymodules.overlap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventGroup, Long> {

    List<EventGroup> findByExpiredAtBefore(LocalDate date);
    EventGroup findByUrl(String url);
    Optional<EventGroup> findFirstByUserOrderByIdDesc(User user);
    boolean existsByUrl(String url);
}
