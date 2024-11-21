package com.jh.coincoin;
import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ApiTest {

    private final String baseUrl = "https://fapi.binance.com";
    private final RestClient restClient = RestClient.create();
    private final CandleService candleService;
    private final RSIIndicator rsiIndicatorService;
    private final AdminService adminService;
    private final CandleCollectorAPIService candleCollectorAPIService;


    @Test
    public void ping(){
        Long serverTime = restClient.get()
                .uri(baseUrl + "/fapi/v1/ping")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Long.class);

        System.out.println(serverTime);
    }

    @Test
    public void intervalCandle() {
//        candleService.manualUpdate(300);

        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleListPerInterval(Symbol.BTC_USDT, interval);

        log.info("================{}분봉================", interval.getMinute());
        for (var keyValue : candleMap.entrySet()) {
            log.info("{} : {}", DateTimeUtil.toDateTime(keyValue.getKey()), keyValue.getValue());
        }
    }

    @Test
    public void rsi() {
//        candleService.manualUpdate(300);

        String rsi = rsiIndicatorService.getLastFigure(Symbol.BTC_USDT, Interval.ONE_MINUTE);

        log.info("RSI : {}", rsi);
    }

    @Test
    public void repoTest() {
        Interval interval = Interval.ONE_MINUTE;
        var candleMap = candleService.getCandleListPerInterval(Symbol.BTC_USDT, interval);

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

        if (interval == BinanceType.Interval.ONE_MINUTE)
            result = true;

        if (interval == BinanceType.Interval.HOUR && minute == 0)
            result = true;

        result = minute % interval.getMinute() == 0;
        log.info("################### : {}", result);
    }

    @Test
    public void setSymbol() {
        adminService.setSymbol(Symbol.TRX_USDT);

        // TODO candle record insert

        candleService.update();
    }

    @Test
    public void GO서버에_심볼_트랙킹하게하기() {
        Symbol symbol = Symbol.BTC_USDT;
        candleCollectorAPIService.orderTrackingSymbol(symbol);
    }
}
