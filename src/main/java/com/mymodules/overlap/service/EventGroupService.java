package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.entity.EventGroup;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.EventRepository;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EventGroupService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Transactional
    public EventGroupResponseDto createEvent(EventGroupRequestDto req, String userId) {
        if(userId == "guest"){
            EventGroupResponseDto res = new EventGroupResponseDto("Guest", req.getTitle(),req.getDates(),req.getStartTime(),req.getEndTime(),req.isCreatorParticipates(),req.getCreatorSelectedTimes());
            System.out.println(res);
            return res;
        } else {
            System.out.println("일정생성요청 사용자 : "+ userId);
            User user = userRepository.findByOauthId(userId);
            String username = user.getUsername();
            EventGroupResponseDto res = new EventGroupResponseDto(username, req.getTitle(),req.getDates(),req.getStartTime(),req.getEndTime(),req.isCreatorParticipates(),req.getCreatorSelectedTimes());
            System.out.println(res);
            return res;
        }
    }
}
