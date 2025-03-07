package com.mymodules.overlap.controller;


import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.service.EventGroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class EventGroupController {
    private final EventGroupService eventGroupService;
    private final JwtUtil jwtUtil;

    @PostMapping("/eventGroup/create")
    public ResponseEntity<EventGroupResponseDto> createEventGroup(HttpServletRequest request, @RequestBody EventGroupRequestDto req){


        String jwtToken = jwtUtil.getJwtFromCookies(request);
        String oauthId = jwtUtil.getSubject(jwtToken);


        EventGroupResponseDto resp = eventGroupService.createEvent(req, oauthId);

        return ResponseEntity.ok(resp);
    }

}
