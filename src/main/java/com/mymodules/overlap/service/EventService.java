package com.mymodules.overlap.service;

import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.dto.SelectedTime;
import com.mymodules.overlap.entity.EventGroup;
import com.mymodules.overlap.entity.TimeTable;
import com.mymodules.overlap.entity.User;
import com.mymodules.overlap.repository.EventRepository;
import com.mymodules.overlap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RequiredArgsConstructor
@Service
public class EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Transactional
    public Object createEvent(EventGroupRequestDto req, String userId) {
        // 유효성 검증
        Map<String, String> errorResponse = new HashMap<>();

        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            errorResponse.put("error", "제목은 필수입력 항목입니다.");
            return errorResponse;
        }

        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            errorResponse.put("error", "닉네임 필드를 채워야 합니다.");
            return errorResponse;
        }

        if (req.getDates() == null || req.getDates().isEmpty()) {
            errorResponse.put("error", "날짜는 최소 1개 이상 선택해야 합니다.");
            return errorResponse;
        }

        if (req.getDates().size() > 31) {
            errorResponse.put("error", "날짜는 최대 31개까지만 선택 가능합니다.");
            return errorResponse;
        }

        if (req.getStartTime() == null || req.getEndTime() == null) {
            errorResponse.put("error", "시작 시간과 종료 시간은 필수입력 항목입니다.");
            return errorResponse;
        }

        if (req.getStartTime().equals(req.getEndTime())) {
            errorResponse.put("error", "시작 시간과 종료 시간이 동일할 수 없습니다.");
            return errorResponse;
        }

        if (req.getStartTime().compareTo(req.getEndTime()) > 0) {
            errorResponse.put("error", "종료 시간은 시작 시간보다 이후여야 합니다.");
            return errorResponse;
        }

        // creatorParticipates가 false인 경우 creatorSelectedTimes를 null로 설정
        if (!req.isCreatorParticipates()) {
            req.setCreatorSelectedTimes(null);
        } else {
            // creatorParticipates가 true인데 creatorSelectedTimes가 null인 경우
            if (req.getCreatorSelectedTimes() == null || req.getCreatorSelectedTimes().isEmpty()) {
                errorResponse.put("error", "참여자가 선택한 시간이 필요합니다.");
                return errorResponse;
            }
        }

        // 유효성 검증 끝
        if(userId.equals("guest")){
            User user = new User(req.getUsername());
            userRepository.save(user);
            System.out.println("게스트 유저\n"+ "\nusername : "+ user.getUsername()+ "\nuuid : " + user.getUuid() +"생성됨");
            
            // EventGroup과 TimeTable 생성 및 연결
            EventGroupResponseDto res = new EventGroupResponseDto("Guest", req.getTitle(),req.getDates(),req.getStartTime(),req.getEndTime(),req.isCreatorParticipates(),req.getCreatorSelectedTimes());
            EventGroup eventGroup = new EventGroup(res, user);
            
            // creatorParticipates가 true면 TimeTable 생성
            if (req.isCreatorParticipates() && req.getCreatorSelectedTimes() != null) {
                TimeTable timeTable = createTimeTable(user, req.getCreatorSelectedTimes());
                eventGroup.setTimeTable(timeTable);
            }
            
            eventRepository.save(eventGroup);
            System.out.println(res);
            return res;
        } else {
            System.out.println("일정생성요청 사용자 : "+ userId);
            User user = userRepository.findByUuid(userId);
            String username = user.getUsername();
            EventGroupResponseDto res = new EventGroupResponseDto(username, req.getTitle(),req.getDates(),req.getStartTime(),req.getEndTime(),req.isCreatorParticipates(),req.getCreatorSelectedTimes());
            EventGroup eventGroup = new EventGroup(res, user);
            
            // creatorParticipates가 true면 TimeTable 생성
            if (req.isCreatorParticipates() && req.getCreatorSelectedTimes() != null) {
                TimeTable timeTable = createTimeTable(user, req.getCreatorSelectedTimes());
                eventGroup.setTimeTable(timeTable);
            }
            
            eventRepository.save(eventGroup);
            System.out.println(res);
            return res;
        }
    }

    // TimeTable 생성 및 선택된 시간 데이터 변환 메서드 추가
    private TimeTable createTimeTable(User user, List<SelectedTime> selectedTimes) {
        // SelectedTime 객체들을 JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDate, LocalTime 직렬화를 위한 모듈
        
        String selectedTimeJson;
        try {
            selectedTimeJson = objectMapper.writeValueAsString(selectedTimes);
        } catch (Exception e) {
            throw new RuntimeException("선택된 시간 데이터 변환 중 오류가 발생했습니다.", e);
        }
        
        return new TimeTable(user, selectedTimeJson);
    }

    private void loadEventGroup(String url) {
        EventGroup eventGroup = eventRepository.findByUrl(url);

        if (eventGroup != null) {
            throw new IllegalArgumentException();
        }


    }
}