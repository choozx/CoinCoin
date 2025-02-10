package com.jh.coincoin;

import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.service.indicator.RSIIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

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
}
