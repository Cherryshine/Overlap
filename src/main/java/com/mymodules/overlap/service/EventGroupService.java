package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.entity.EventGroup;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.EventRepository;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventGroupService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Transactional
    public EventGroupResponseDto createEvent(EventGroupRequestDto req, String userId) {
        if("guest".equals(userId)){
            EventGroupResponseDto res = new EventGroupResponseDto("guest", req);
            System.out.println(res);
            return res;
        } else {
            System.out.println("일정생성요청 사용자 : "+ userId);
            User user = userRepository.findByOauthId(userId);
            String username = user.getUsername();
            EventGroupResponseDto res = new EventGroupResponseDto(username, req);
            EventGroup eventGroup = new EventGroup(res,user);
            eventRepository.save(eventGroup);
            System.out.println(res);
            return res;
        }
    }

    // EventGroup 만료된 일정 삭제 메소드
    @Transactional
    public void deleteEventGroup() {

        // 오늘 날짜 가져오기
        LocalDate today = LocalDate.now();
        // EventGroup 에서 오늘 날짜보다 이전인 expiredAt 전부 가져오기
        List<EventGroup> expiredEventList = eventRepository.findByExpiredAtBefore(today);
        log.info("만료된 이벤트 그룹 개수 {}" , expiredEventList);
        // EventGroup 에서 삭제
        eventRepository.deleteAll(expiredEventList);
        log.info("만료된 이벤트 삭제 완료");
    }
}
