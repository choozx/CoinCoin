package com.jh.coincoin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Strategy;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.service.strategy.order.ReverseTrendUsingRsiOrderStrategy;
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
public class OrderStrategyTest {

    private final ReverseTrendUsingRsiOrderStrategy reverseTrendUsingRsiOrderStrategy;

    @Test
    public void 역추세_신호_리스트() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        Strategy.RSIValue rsiValue = Strategy.RSIValue.builder()
                .overBought(80)
                .overSell(20)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String stringRsiValue;
        try {
            stringRsiValue = objectMapper.writeValueAsString(rsiValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        long end = DateTimeUtil.getCurrentTimeMillis();
        long begin = DateTimeUtil.calcBeginTime(end, interval.getMinute(), 1000);

        log.info("시작 시간 {}", DateTimeUtil.toDateTime(begin));
        log.info("종료 시간 {}", DateTimeUtil.toDateTime(end));

        var hitList = reverseTrendUsingRsiOrderStrategy.getHitList(symbol, interval, stringRsiValue, begin, end);

        for (var pair: hitList) {
            log.info("신호 발생! {} : {}", DateTimeUtil.toDateTime(pair.getLeft()), pair.getRight());
        }
    }
}
