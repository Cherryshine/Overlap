package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.entity.EventGroupEntity;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.EventRepository;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EventGroupService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Transactional
    public EventGroupResponseDto createEvent(EventGroupRequestDto req, String oauthId) {

        User user = userRepository.findByOauthId(oauthId);

        return null;
    }
}



