package com.jh.coincoin;
import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.SlackService;
import com.jh.coincoin.service.indicator.RSIIndicatorService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ApiTest {

    private final String baseUrl = "https://fapi.binance.com";
    private final RestClient restClient = RestClient.create();
    private final SlackService slackService;
    private final CandleService candleService;
    private final RSIIndicatorService rsiIndicatorService;


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
    public void sendMessage(){
        Map<String, String> message = new HashMap<>();
        message.put("중본문", "소본문");
        slackService.sendMessage("큰본문", message);
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
}
