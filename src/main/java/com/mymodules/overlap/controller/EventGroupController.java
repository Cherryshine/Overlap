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

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class EventGroupController {
    private final EventService eventService;
    private final JwtUtil jwtUtil;

    @PostMapping("/schedules")
    public ResponseEntity<EventGroupResponseDto> createSchedule(@RequestBody EventGroupRequestDto requestDto, HttpServletRequest request) {

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

        EventGroupResponseDto responseDto = eventService.createEvent(requestDto, oauthId);
        System.out.println("returning HTTP STATUS 201 Created");

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
