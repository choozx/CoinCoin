package com.jh.coincoin;

import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.indicator.RSIIndicator;
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
public class IndicatorServiceTest {

    private final RSIIndicator rsiIndicator;

    @Test
    public void rsi_지표_업데이트() {
        rsiIndicator.update(Symbol.BTCUSDT, Interval.FIVE_MINUTE);

        double value = rsiIndicator.getLastFigure(Symbol.BTCUSDT, Interval.FIVE_MINUTE);
        log.info("rsi value : {}", value);
    }
}
