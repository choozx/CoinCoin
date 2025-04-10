package com.jh.coincoin;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.*;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class CandleServiceTest {

    private final CandleService candleService;
    private final CandleCollectorAPIService candleCollectorAPIService;

    @Test
    public void 과거캔들_200개_포함해서_가져오기() {
        Symbol symbol = Symbol.BTCUSDT;
        Interval interval = Interval.FIVE_MINUTE;

        long lastOpenTime = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime now = DateTimeUtil.toDateTime(lastOpenTime);
        log.info("지금 시간 : {}", now);
        LocalDateTime nextOpenTime = now.plusMinutes(interval.getMinute());

        log.info("다음 분봉 : {}", nextOpenTime);
        LocalDateTime targetLocaltime = nextOpenTime.minusMinutes((long) interval.getMinute() * 200);
        log.info("타겟 시간 : {}", targetLocaltime);

        long beginTime = DateTimeUtil.toEpochMilli(targetLocaltime); // rsi값을 구하기 위해서는 200개의 캔들이 필요
        var candleMap = candleService.getCandleMap(symbol, interval, beginTime);

        log.info("candleMap.size() : {}", candleMap.size());
        log.info("처음 캔들 시작 시간:{}", DateTimeUtil.toDateTime(candleMap.lastKey()));
        log.info("마지막 캔들 시작 시간:{}", DateTimeUtil.toDateTime(candleMap.firstKey()));
    }

    @Test
    public void 시간_올림_내림() {
        Interval interval = Interval.FIVE_MINUTE;

        long nowLong = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime now = DateTimeUtil.toDateTime(nowLong);
        log.info("현재:{}", now);

        long ceil = DateTimeUtil.ceilToInterval(nowLong, interval.getMinute());
        long floor = DateTimeUtil.floorToInterval(nowLong, interval.getMinute());

        log.info("올림 : {}", DateTimeUtil.toDateTime(ceil));
        log.info("내림 : {}", DateTimeUtil.toDateTime(floor));

        // 정시를 올림 내림
        long tmp1 = DateTimeUtil.ceilToInterval(ceil, interval.getMinute());
        long tmp2 = DateTimeUtil.floorToInterval(ceil, interval.getMinute());
        log.info("정시 올림 :{}", DateTimeUtil.toDateTime(tmp1));
        log.info("정시 내림 :{}", DateTimeUtil.toDateTime(tmp2));
    }

    @Test
    public void 캔들_가져오기() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        var candleMap = candleService.getCandleMap(symbol, interval);

        var candle = candleMap.lastEntry();
        log.info("시간 : {} | last candle : {}", DateTimeUtil.toDateTime(candle.getKey()), candle);
    }

    @Test
    public void 캔들_DB에서_count만큼_가져오기() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        int candleCount = 100;
        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime beginDateTime = DateTimeUtil.toDateTime(now).minusMinutes(1000);

        long begin = DateTimeUtil.toEpochMilli(beginDateTime);
        log.info("시작 시간:{}", beginDateTime);
        var candleMap = candleService.getCandleMapByBeginToDB(symbol, interval, begin, candleCount);

        log.info("캔들 사이즈 {}", candleMap.size());
        var firstCandle = candleMap.firstEntry();
        var lastCandle = candleMap.lastEntry();
        log.info("시간 : {} | first candle : {}", DateTimeUtil.toDateTime(firstCandle.getKey()), firstCandle);
        log.info("시간 : {} | last candle : {}", DateTimeUtil.toDateTime(lastCandle.getKey()), lastCandle);
    }

    @Test
    public void 캔들_DB에서_가져오기() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime beginDateTime = DateTimeUtil.toDateTime(now).minusMinutes(1000L * interval.getMinute());

        long beginTime = adjustBeginTime(DateTimeUtil.toEpochMilli(beginDateTime), interval); // rsi값을 구하기 위해서는 200개의 캔들이 필요
        log.info("시작 시간:{}", beginDateTime);
        TreeMap<Long, Candle> candleMap = candleService.getCandleMapToDB(symbol, interval, beginTime, now);

        log.info("캔들 사이즈 {}", candleMap.size());
        var firstCandle = candleMap.firstEntry();
        var lastCandle = candleMap.lastEntry();
        log.info("시간 : {} | first candle : {}", DateTimeUtil.toDateTime(firstCandle.getKey()), firstCandle);
        log.info("시간 : {} | last candle : {}", DateTimeUtil.toDateTime(lastCandle.getKey()), lastCandle);
    }

    @Test
    public void 캔들_하나_가져오기() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime entryLocalTime = DateTimeUtil.toDateTime(now).minusMinutes(100);
        long entryTime = DateTimeUtil.toEpochMilli(entryLocalTime);

        log.info("시작시간:{}", entryLocalTime);

        var candle = candleService.getCandleMap(symbol, interval, entryTime, DateTimeUtil.calcEndTime(entryTime, interval.getMinute(), 1));

        log.info("size:{}", candle.size());
        log.info("캔들시간:{} {}", DateTimeUtil.toDateTime(candle.firstKey()), candle.firstEntry().getValue());
    }

    @Test
    public void 과거_캔들_업데이트_시작() {
        Symbol symbol = Symbol.BTCUSDT;
        candleCollectorAPIService.startCollectPastCandle(symbol);
    }

    @Test
    public void DB_에서_마지막_캔들_추출() {
        Symbol symbol = Symbol.ETHUSDT;
        Interval interval = Interval.FIVE_MINUTE;
        var lastCandle = candleService.getLastCandleToDB(symbol, interval);

        log.info("지금 시간:{}", DateTimeUtil.toDateTime(DateTimeUtil.getCurrentTimeMillis()));
        log.info("Symbol:{} | 오픈시간:{} | 캔들:{}", lastCandle.getSymbol(), DateTimeUtil.toDateTime(lastCandle.getOpenTime()), lastCandle);
    }

    private long adjustBeginTime(long begin, Interval interval) {
        LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(begin).plusMinutes(interval.getMinute());
        return DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * 200)); // rsi값을 구하기 위해서는 200개의 캔들이 필요
    }
}
