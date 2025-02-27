package com.jh.coincoin;

import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.Strategy;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.PositionSide;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.IndicatorService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.service.strategy.buy.calculator.RiskRewardCalculator;
import com.jh.coincoin.util.DateTimeUtil;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private Map<StrategyType.RiskRewardRatioType, RiskRewardCalculator> riskRewardCalculatorMap;

    @Autowired
    public void setRiskRewardCalculatorMap(Set<RiskRewardCalculator> riskRewardCalculatorSet) {
        this.riskRewardCalculatorMap = riskRewardCalculatorSet.stream().collect(Collectors.toMap(RiskRewardCalculator::getType, Function.identity()));
    }


    @Test
    public void intervalCandle() {
//        candleService.manualUpdate(300);

        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleMap(Symbol.BTCUSDT, interval);

        log.info("================{}분봉================", interval.getMinute());
        for (var keyValue : candleMap.entrySet()) {
            log.info("{} : {}", DateTimeUtil.toDateTime(keyValue.getKey()), keyValue.getValue());
        }
    }

    @Test
    public void rsi() {
//        candleService.manualUpdate(300);

        Double rsi = rsiIndicatorService.getLastValue(Symbol.BTCUSDT, Interval.ONE_MINUTE);

        log.info("RSI : {}", rsi);
    }

    @Test
    public void repoTest() {
        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleMap(Symbol.BTCUSDT, interval);

//        candleService.saveCandle(candleMap.values().stream().toList());
    }

//    @Test
//    public void timeTest() {
//        long now = DateTimeUtil.getCurrentTimeMillis();
//        LocalDateTime nowLocal = DateTimeUtil.toDateTime(now);
//        log.info("now : {}", nowLocal);
//
//        LocalDateTime truncate = nowLocal.truncatedTo();
//        log.info("truncated : {}", truncate);
//
//        LocalDateTime minus = truncate.minusMinutes(1000);
//        log.info("minus : {}", minus);
//
//        long epochMilli = DateTimeUtil.toEpochMilli(minus);
//        log.info("result : {}", epochMilli);
//    }

    @Test
    public void cccc() {
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
        indicatorService.detectIndicator(Interval.FIFTEEN_MINUTE);
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
    public void 손익_가격_계산() {
        StrategyType.RiskRewardRatioType type = StrategyType.RiskRewardRatioType.PEAK_RATIO;
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;

        Candle candle = candleService.getLastCandle(symbol, interval);
        log.info("캔들 : {}", candle);

        double price = riskRewardCalculatorMap.get(type).calcPrice(Strategy.PriceCalculatorDto.builder()
                .entryPrice(candle.getClosePrice())
                .riskRewardRatio(2)
                .interval(interval)
                .order(Order.STOP_MARKET)
                .symbol(symbol)
                .side(Side.SELL)
                .build());

        log.info("계산된 가격: {}", price);
    }

    @Test
    public void 센트리_테스트() {
        try {
            int a = 10 / 0;
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
