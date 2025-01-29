package com.jh.coincoin;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.PositionSide;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.IndicatorService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ApiTest {

    private final CandleService candleService;
    private final RSIIndicator rsiIndicatorService;
    private final IndicatorService indicatorService;
    private final AdminService adminService;
    private final CandleCollectorAPIService candleCollectorAPIService;


    @Test
    public void intervalCandle() {
//        candleService.manualUpdate(300);

        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleListPerInterval(Symbol.BTCUSDT, interval);

        log.info("================{}분봉================", interval.getMinute());
        for (var keyValue : candleMap.entrySet()) {
            log.info("{} : {}", DateTimeUtil.toDateTime(keyValue.getKey()), keyValue.getValue());
        }
    }

    @Test
    public void rsi() {
//        candleService.manualUpdate(300);

        Double rsi = rsiIndicatorService.getLastFigure(Symbol.BTCUSDT, Interval.ONE_MINUTE);

        log.info("RSI : {}", rsi);
    }

    @Test
    public void repoTest() {
        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleListPerInterval(Symbol.BTCUSDT, interval);

//        candleService.saveCandle(candleMap.values().stream().toList());
    }

    @Test
    public void timeTest() {
        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime nowLocal = DateTimeUtil.toDateTime(now);
        log.info("now : {}", nowLocal);

        LocalDateTime truncate = nowLocal.truncatedTo(ChronoUnit.MINUTES);
        log.info("truncated : {}", truncate);

        LocalDateTime minus = truncate.minusMinutes(1000);
        log.info("minus : {}", minus);

        long epochMilli = DateTimeUtil.toEpochMilli(minus);
        log.info("result : {}", epochMilli);
    }

    @Test
    public void cccc(){
        Interval interval = Interval.FIVE_MINUTE;
        boolean result = false;
        int minute = LocalDateTime.now().getMinute();

        if (interval == Interval.ONE_MINUTE)
            result = true;

        if (interval == Interval.HOUR && minute == 0)
            result = true;

        result = minute % interval.getMinute() == 0;
        log.info("################### : {}", result);
    }

    @Test
    public void setSymbol() {
        adminService.setSymbol(Symbol.TRXUSDT);

        // TODO candle record insert

        candleService.update();
    }

    @Test
    public void GO서버에_심볼_트랙킹하게하기() {
        Symbol symbol = Symbol.BTCUSDT;
        candleCollectorAPIService.orderTrackingSymbol(symbol);
    }

    @Test
    public void 지표감지() {
        indicatorService.detectIndicator();
    }

    @Test
    public void 쿼리_스트링으로_변환() {
        NewOrderReq req = NewOrderReq.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .positionSide(PositionSide.BOTH)
                .type(Order.MARKET)
                .timestamp(DateTimeUtil.getCurrentTimeMillis())
                .build();

        String queryString = req.toQueryString();

        log.info("{}", queryString);
    }

    @Test
    public void 나누기_테스트() {
        Pair<Double, Double> riskRewardRatio = adminService.getRiskRewardRatio();
        double rewardRatio = riskRewardRatio.getRight();
        double riskRatio = riskRewardRatio.getLeft();
        double avgPrice = 61000;
        log.info("{}", avgPrice - (avgPrice * riskRatio / 100));
    }

    @Test
    public void 타겟_가격_계산() {
        Order order = Order.STOP_MARKET;
        double initPrice = 1000.0;
        Side side = Side.SELL;

        Pair<Double, Double> riskRewardRatio = adminService.getRiskRewardRatio();

//        double decidePrice = CommonUtil.calcPrice(side, order, initPrice, riskRewardRatio.getRight());

//        log.info("decide price : {}", decidePrice);
    }
}
