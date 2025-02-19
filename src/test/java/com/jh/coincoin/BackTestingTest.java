package com.jh.coincoin;

import com.jh.coincoin.model.BackTest;
import com.jh.coincoin.model.BackTest.BackTestBuyDto;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.service.BackTestService;
import com.jh.coincoin.util.BinanceUtil;
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
public class BackTestingTest {

    private final BackTestService backTestService;

    @Test
    public void 손익계산() {
        double candlePrice = 975;
        double balance = 1000;
        int leverage = 10;
        double orderRatio = 0.1;
        BackTestBuyDto dto = BackTestBuyDto.builder()
                .entryTime(1000000)
                .avgPrice(1000)
                .limitPrice(980)
                .stopPrice(1040)
                .build();

        BackTest.PnlDto pnlDto = calcPnl(candlePrice, dto, balance, leverage, orderRatio);
        log.info("수익률:{}", pnlDto);
    }

    @Test
    public void 청산가_계산() {
            Side side = Side.BUY;
            double entryPrice = 100000;
            int leverage = 10;
            double liqPrice = BinanceUtil.calcLiquidationPrice(side, entryPrice, leverage);

        log.info("청산가: {}", liqPrice);
    }

    @Test
    public void 백테스트() {
        int strategyIdx = 1;
        long end = DateTimeUtil.getCurrentTimeMillis();
        long begin = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(end).minusMonths(1));
        double initBalance = 1000;

        backTestService.backTest(strategyIdx, begin, end, initBalance);
    }

    private BackTest.PnlDto calcPnl(double closePrice, BackTestBuyDto backTestBuyDto, double balance, int leverage, double orderBalanceRatio) {
        double avgPrice = backTestBuyDto.getAvgPrice();
        double limitPrice = backTestBuyDto.getLimitPrice();
        double stopPrice = backTestBuyDto.getStopPrice();
        double entryBalance = balance * orderBalanceRatio;

        Side side = backTestBuyDto.getLimitPrice() > avgPrice ? Side.BUY : Side.SELL;
        double positionSize = (balance * orderBalanceRatio * leverage) / avgPrice; // 포지션 크기 계산

        log.info("수량:{}", positionSize);
        double priceDiff = 0;
        double closePosition = 0;
        if (side == Side.BUY) {
            if (closePrice > limitPrice){
                priceDiff = limitPrice - avgPrice;
                closePosition = limitPrice;
            }
            if (closePrice < stopPrice) {
                priceDiff = stopPrice - avgPrice;
                closePosition = stopPrice;
            }
        } else {
            if (closePrice < limitPrice) {
                priceDiff = avgPrice - limitPrice;
                closePosition = limitPrice;
            }
            if (closePrice > stopPrice) {
                priceDiff = avgPrice - stopPrice;
                closePosition = stopPrice;
            }
        }

        double pnl = priceDiff * positionSize; // 손익 계산
        double pnlPercentage = (pnl / entryBalance) * 100; // 손익률 계산
        double pnlByBalance = (pnl / balance) * 100;

        return BackTest.PnlDto.builder()
                .avgPrice(avgPrice)
                .closePrice(closePosition)
                .pnl(pnl)
                .pnlPercentage(pnlPercentage)
                .pnlPercentageByBalance(pnlByBalance)
                .build();
    }

    @Test
    public void 시간_변환_테스트() {
        String dateString = "2025-01-22";
        long time = DateTimeUtil.convertDateStringToEpoch(dateString, DateTimeUtil.YYYY_MM_DD_FMT);

        LocalDateTime dateTime = DateTimeUtil.toDateTime(time);

        log.info("{} == {}", time, dateTime);
    }
}
