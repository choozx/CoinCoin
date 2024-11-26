package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * Created by dale on 2024-09-11.
 */

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final CandleService candleService;
    private final AdminService adminService;
    private final IndicatorService indicatorService;
    private final TradeService tradeService;

    // 매분 5초 때마다 실행
    @Scheduled(cron = "5 * * * * *")
    public void update(){
        candleService.update();

        BinanceType.Interval interval = adminService.getInterval();
        if (timeChecker(interval)) {
            indicatorService.detectIndicator();
            tradeService.trade();
        }
    }

    private boolean timeChecker(BinanceType.Interval interval) {
        int minute = LocalDateTime.now().getMinute();

        if (interval == BinanceType.Interval.ONE_MINUTE)
            return true;

        if (interval == BinanceType.Interval.HOUR && minute == 0)
            return true;

        return minute % interval.getMinute() == 0;
    }
}
