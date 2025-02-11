package com.jh.coincoin;

import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.TreeMap;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class IndicatorServiceTest {

    private final RSIIndicator rsiIndicator;

    @Test
    public void rsi_지표_업데이트() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        rsiIndicator.update(symbol, interval);

        double value = rsiIndicator.getLastValue(symbol, interval);
    }

    @Test
    public void rsi_지표_맵_기져오기() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.ONE_MINUTE;
        long end = DateTimeUtil.getCurrentTimeMillis();
        long begin = DateTimeUtil.calcBeginTime(end, interval.getMinute(), 100);

        log.info("시작 시간 {}", DateTimeUtil.toDateTime(begin));
        log.info("종료 시간 {}", DateTimeUtil.toDateTime(end));

        TreeMap<Long, Double> rsiMap = rsiIndicator.getValueMap(symbol, interval, begin, end);

        log.info("rsi :: 시작시간:{} 값:{}", DateTimeUtil.toDateTime(rsiMap.firstKey()), rsiMap.firstEntry().getValue());
        log.info("rsi :: 종료시간:{} 값:{}", DateTimeUtil.toDateTime(rsiMap.lastKey()), rsiMap.lastEntry().getValue());
    }
}
