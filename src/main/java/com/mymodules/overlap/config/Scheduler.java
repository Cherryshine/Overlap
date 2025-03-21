package com.mymodules.overlap.config;

import com.mymodules.overlap.service.EventGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final EventGroupService eventGroupService;

    // 매일 00시 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void requestDeletExpiredEvent(){
        log.info("스케쥴러 실행");
        eventGroupService.deleteEventGroup();
    }


}
