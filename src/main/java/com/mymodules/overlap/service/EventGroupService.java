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

        //        EventGroupEntity event = new EventGroupEntity();
//        event.setUser(user);
//        event.setTitle(req.getTitle());
//        event.setStartTime(req.getStartTime());
//        event.setEndTime(req.getEndTime());
//
//
//        List<Map<String, Integer>> dateList = req.getSelectedDates();
//
//        if (dateList != null && !dateList.isEmpty()) {
//            List<String> dateStrList = dateList.stream()
//                    .map(d -> d.get("year") + "-" + d.get("month") + "-" + d.get("day"))
//                    .collect(Collectors.toList());
//
//            String joined = String.join(",", dateStrList);
//            event.setSelectDates(joined);
//        } else {
//            event.setSelectDates("");
//        }
//
//
//        EventGroupEntity eventGroup = eventRepository.save(event);
//
//
//        return new EventGroupResponseDto(eventGroup);


// ✅ 가장 최근에 생성된 EventGroupEntity를 가져온다.
        Optional<EventGroupEntity> existingEventOpt = eventRepository.findFirstByUserOrderByIdDesc(user);

// ✅ 선택된 날짜 리스트를 ","로 구분된 문자열로 변환
        List<Map<String, Integer>> dateList = req.getSelectedDates();
        String joinedDates = (dateList != null && !dateList.isEmpty())
                ? dateList.stream()
                .map(d -> d.get("year") + "-" + d.get("month") + "-" + d.get("day")) // 연-월-일 형태로 변환
                .collect(Collectors.joining(",")) // 쉼표(,)로 연결된 문자열 생성
                : ""; // 날짜가 없을 경우 빈 문자열 처리

// ✅ 기존 데이터가 존재하는 경우
        if (existingEventOpt.isPresent()) {
            EventGroupEntity existingEvent = existingEventOpt.get(); // 기존 이벤트 객체 가져오기

            // ✅ 기존 데이터와 새로 입력된 데이터가 완전히 동일한지 비교
            boolean isSameData = existingEvent.getTitle().equals(req.getTitle()) &&
                    existingEvent.getStartTime().equals(req.getStartTime()) &&
                    existingEvent.getEndTime().equals(req.getEndTime()) &&
                    existingEvent.getSelectDates().equals(joinedDates); // 선택된 날짜 비교

            // ✅ 기존 데이터와 동일하다면 추가 저장 없이 기존 데이터를 반환
            if (isSameData) {
                return new EventGroupResponseDto(existingEvent);
            }

            // ✅ 기존 데이터가 변경되었으면 업데이트 수행
            existingEvent.updateEvent(req.getTitle(), req.getStartTime(), req.getEndTime(), joinedDates);
            eventRepository.save(existingEvent); // 업데이트된 데이터를 저장

            return new EventGroupResponseDto(existingEvent); // 업데이트된 이벤트 반환
        }

// ✅ 기존 데이터가 없을 때만 새 데이터 생성 및 저장
        EventGroupEntity newEvent = new EventGroupEntity(user, req.getTitle(), req.getStartTime(), req.getEndTime(), joinedDates);
        eventRepository.save(newEvent); // 새로운 데이터 저장

        return new EventGroupResponseDto(newEvent); // 새로 생성된 이벤트 반환
    }
}




