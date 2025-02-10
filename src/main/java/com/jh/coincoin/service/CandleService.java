package com.jh.coincoin.service;

import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.repo.CandleRepository;
import com.jh.coincoin.util.DateTimeUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.jh.coincoin.model.consts.GlobalConst.MAX_STORAGE_CANDLE_COUNT;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

    private final AdminService adminService;
    private final CandleRepository candleRepository;

    private Map<Symbol, TreeMap<Long, Candle>> allSymbolMap = new HashMap<>();

    @PostConstruct
    public void init() {
        allSymbolLoad2DB();
    }

    public TreeMap<Long, Candle> getCandleMapByBeginAndCount(Symbol symbol, Interval interval, long begin, int candleCount) {
        long plusMinute = (long) interval.getMinute() * candleCount;
        long end = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(begin).plusMinutes(plusMinute));
        return getCandleMap(symbol, interval, begin, end);
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval) {
        var candleMap = allSymbolMap.get(symbol);
        return getCandleMap(symbol, interval, candleMap.lastKey(), candleMap.firstKey());
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval, int candleCount) {
        long beginTime = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime endDateTime = DateTimeUtil.toDateTime(beginTime);

        long minusMinute = (long) interval.getMinute() * candleCount;
        endDateTime.minusMinutes(minusMinute);
        long endTime = DateTimeUtil.toEpochMilli(endDateTime);

        return getCandleMap(symbol, interval, beginTime, endTime);
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval, long beginTime) {
        return getCandleMap(symbol, interval, beginTime, DateTimeUtil.getCurrentTimeMillis());
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval, long beginTime, long endTime) {
        TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);
        beginTime = Math.max(beginTime, candleMap.firstKey());

        long roundBeginTime = DateTimeUtil.ceilToInterval(beginTime, interval.getMinute());
        long floorEndTime = DateTimeUtil.floorToInterval(endTime, interval.getMinute());
        var subCandleMap = candleMap.subMap(roundBeginTime, floorEndTime);

        TreeMap<Long, Candle> candleMapPerInterval = new TreeMap<>();
        for (var candleEntry : subCandleMap.entrySet()) {
            long timestamp = candleEntry.getKey();
            Candle candle = candleEntry.getValue();

            long floorTime = DateTimeUtil.floorTimestamp(timestamp, interval.getMinute());

            if (candleMapPerInterval.containsKey(floorTime)) {
                Candle existingCandle = candleMapPerInterval.get(floorTime);
                existingCandle.updateCandle(candle);
            } else {
                candleMapPerInterval.put(floorTime, new Candle(candle));
            }
        }

        return candleMapPerInterval;
    }

    public Candle getLastCandle(Symbol symbol, Interval interval) {
        TreeMap<Long, Candle> lastCandleMap = getCandleMap(symbol, interval);
        return lastCandleMap.firstEntry().getValue();
    }

    public void update() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime targetTime = DateTimeUtil.toDateTime(now).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT);
        for (Symbol symbol : symbolList) {
            long lastOpenTime = DateTimeUtil.toEpochMilli(targetTime);
            TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);
            if (candleMap != null && candleMap.size() != 0)
                lastOpenTime = candleMap.firstKey();

            load2DB(symbol, lastOpenTime);
        }

        log.info("캔들 로드 완료");
    }

    public void removeTrackingCandle(Symbol symbol) {
        allSymbolMap.remove(symbol);
    }

    private void allSymbolLoad2DB() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        LocalDateTime targetTime = DateTimeUtil.toDateTime(DateTimeUtil.getCurrentTimeMillis()).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT);
        long targetTimestamp = DateTimeUtil.toEpochMilli(targetTime);
        for (Symbol symbol : symbolList) {
            load2DB(symbol, targetTimestamp);
        }
    }

    private void load2DB(Symbol symbol, long targetTimestamp) {
        List<CandleEntity> candleEntityList = candleRepository.findAllByOpenTimeAfterAndSymbol(targetTimestamp, symbol);
        TreeMap<Long, Candle> candleMap = allSymbolMap.getOrDefault(symbol, new TreeMap<>());
        candleEntityList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

        while (candleMap.size() > MAX_STORAGE_CANDLE_COUNT) {
            candleMap.pollFirstEntry();
        }

        allSymbolMap.put(symbol, candleMap);
        log.info("DB 로드 - {}:{}", symbol, candleEntityList.size());
    }
}
