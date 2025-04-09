package com.mymodules.overlap.controller;

import com.mymodules.overlap.config.JwtUtil;
import com.mymodules.overlap.dto.EventGroupRequestDto;
import com.mymodules.overlap.dto.EventGroupResponseDto;
import com.mymodules.overlap.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class EventController {
    private final EventService eventService;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/schedules")
    public ResponseEntity<?> createEvent(@RequestBody EventGroupRequestDto requestDto, 
                                        HttpServletRequest request) {
        String oauthId = (String) request.getSession().getAttribute("id");
        if (oauthId == null) {
            oauthId = "guest";
        }
        System.out.println("컨트롤러 진입 성공");
        Object result = eventService.createEvent(requestDto, oauthId);
        System.out.println("서비스까지 진입 성공");
        // 유효성 검증 실패한 경우 (JSON, Map 타입으로 반환)
        if (result instanceof Map) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        
        // 정상 처리된 경우 - URL로 리다이렉트
        String url = (String) result;
        
        // 리다이렉트 응답 생성 - 절대 경로 사용
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/schedules/" + url));
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Found 상태코드로 리다이렉트
    }

//    리다이렉트 url 맵핑
    @GetMapping("/schedules/{url}")
    public ModelAndView getScheduleTemplate(@PathVariable(required = false) String url) {
        return new ModelAndView("show-schedule");
    }

//    각 url 별 상세스케쥴 페이지 - in develop
    @GetMapping("/api/schedules/timetable/{url}")
    public ResponseEntity<?> getTimeTable(@PathVariable String url) {
        return null;
    }
}
