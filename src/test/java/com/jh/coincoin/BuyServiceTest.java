package com.jh.coincoin;

import com.jh.coincoin.model.Binance;
import com.jh.coincoin.model.Strategy;
import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.service.BuyService;
import com.jh.coincoin.service.TradeLogService;
import com.jh.coincoin.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class BuyServiceTest {

    private final BuyService buyService;
    private final StrategyService strategyService;
    private final TradeLogService tradeLogService;

    @Test
    public void testBuyPosition() {
        List<Integer> matchingIntervalList = new ArrayList<>();
        matchingIntervalList.add(15);
        List<Strategy.TradeStrategyDto> tradeStrategyDtoList = strategyService.getTradeStrategyListByInterval(matchingIntervalList);

        BinanceType.Side side = BinanceType.Side.BUY;
        Binance.BuyResultDto resultDto = buyService.buyPosition(side, tradeStrategyDtoList.getFirst());

        tradeLogService.loggingPosition(resultDto);
        log.info("resultDto={}", resultDto.toString());
    }

}
