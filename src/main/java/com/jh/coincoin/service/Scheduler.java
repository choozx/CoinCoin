package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.service.external.BinanceAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Created by dale on 2024-09-11.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class Scheduler {

    private final CandleService candleService;
    private final AdminService adminService;
    private final IndicatorService indicatorService;
    private final TradeService tradeService;
    private final BinanceAPIService binanceAPIService;

    // 매분 5초 때마다 실행
    @Scheduled(cron = "5 * * * * *")
    public void update(){
        candleService.update();
        indicatorService.update();

        Interval interval = adminService.getInterval();
        if (timeChecker(interval))
            indicatorService.detectIndicator(interval);

        if (adminService.isOnAutoTrade())
            tradeService.tradeV2();
    }

    @Scheduled(cron = "0 */20 * * * *")
    public void updateSocket(){
         binanceAPIService.updateListenKey();
         log.info("웹 소켓 만료기간 업데이트");
    }

    private boolean timeChecker(Interval interval) {
        int minute = LocalDateTime.now().getMinute();

        if (interval == Interval.ONE_MINUTE)
            return true;

        if (interval == Interval.HOUR && minute == 0)
            return true;

        return minute % interval.getMinute() == 0;
    }
}
