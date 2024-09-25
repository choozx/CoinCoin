package com.jh.coincoin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * Created by dale on 2024-09-11.
 */

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final CandleService candleService;
    private final SlackService slackService;

    // 매분 5초 때마다 실행
    @Scheduled(cron = "2 * * * * *")
    public void update(){
        candleService.update();

        slackService.sendAlert();
    }
}
