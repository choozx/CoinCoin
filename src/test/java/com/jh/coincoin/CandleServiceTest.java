package com.jh.coincoin;

import com.jh.coincoin.model.type.BinanceType.*;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class CandleServiceTest {

    private final CandleService candleService;

    @Test
    public void 과거캔들_200개_포함해서_가져오기() {
        Symbol symbol = Symbol.BTCUSDT;
        Interval interval = Interval.FIVE_MINUTE;

        long lastOpenTime = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(lastOpenTime).plusMinutes(interval.getMinute());
        long targetTime = DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * 200)); // rsi값을 구하기 위해서는 200개의 캔들이 필요

        var candleMap = candleService.getCandleMap(symbol, interval, targetTime);

        log.info("candleMap.size() : {}", candleMap.size());
    }
}
