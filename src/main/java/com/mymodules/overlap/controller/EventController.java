package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class EventController {
    private final EventService eventService;
    private final JwtUtil jwtUtil;

    @PostMapping("/schedules")
    public ResponseEntity<?> createSchedule(@RequestBody EventGroupRequestDto requestDto, HttpServletRequest request) {

        String token = jwtUtil.getJwtFromCookies(request);
        String oauthId = jwtUtil.getSubject(token);
        System.out.println(oauthId);

        System.out.println("=== Request Body ===");
        System.out.println("Title: " + requestDto.getTitle());
        System.out.println("Username: " + requestDto.getUsername());
        System.out.println("Dates: " + requestDto.getDates());
        System.out.println("Start Time: " + requestDto.getStartTime());
        System.out.println("End Time: " + requestDto.getEndTime());
        System.out.println("Creator Participates: " + requestDto.isCreatorParticipates());
        System.out.println("Creator Participating times: " + requestDto.getCreatorSelectedTimes());
        System.out.println("==================");

        Object result = eventService.createEvent(requestDto, oauthId);
        
        // 유효성 검증 실패한 경우 (JSON, Map 타입으로 반환)
        if (result instanceof Map) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        
        // 정상 처리된 경우
        EventGroupResponseDto responseDto = (EventGroupResponseDto) result;
        System.out.println("returning HTTP STATUS 201 Created");

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @GetMapping("/schedules/{url}")
    public ResponseEntity<?> getEventGroupByUrl(@PathVariable String url) {
        // eventService.
        // EventGroupResponseDto eventGroup = eventRepository.findEventGroupByUrl(url);
        
        // if (eventGroup == null) {
        //     return new ResponseEntity<>("존재하지 않는 일정입니다.", HttpStatus.NOT_FOUND);
        // }
        
        // return new ResponseEntity<>(eventGroup, HttpStatus.OK);
        return null;
    }
}
