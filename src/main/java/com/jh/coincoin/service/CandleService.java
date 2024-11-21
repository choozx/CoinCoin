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

    public Map<Long, Candle> getCandleListPerInterval(Symbol symbol, Interval interval) {
        var candleMap = allSymbolMap.get(symbol);
        return getCandleListPerInterval(symbol, interval, candleMap.size());
    }

    public Map<Long, Candle> getCandleListPerInterval(Symbol symbol, Interval interval, int candleCount) {
        Map<Long, Candle> candleMap = allSymbolMap.get(symbol);

        Map<Long, Candle> candleMapPerInterval = new TreeMap<>(Comparator.reverseOrder());
        long lastTime = DateTimeUtil.roundTimestamp(System.currentTimeMillis(), interval.getMinute());
        for (var candleEntry : candleMap.entrySet()) {
            if (candleEntry.getKey() >= lastTime)
                continue;

            long timestamp = candleEntry.getKey();
            Candle candle = candleEntry.getValue();

            long roundTime = DateTimeUtil.roundTimestamp(timestamp, interval.getMinute());

            if (candleMapPerInterval.containsKey(roundTime)) {
                Candle existingCandle = candleMapPerInterval.get(roundTime);
                existingCandle.updateCandle(candle);
            } else {
                candleMapPerInterval.put(roundTime, new Candle(candle));
            }

            if (candleMapPerInterval.size() > candleCount)
                break;
        }

        return candleMapPerInterval;
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
        TreeMap<Long, Candle> candleMap = allSymbolMap.getOrDefault(symbol, new TreeMap<>(Comparator.reverseOrder()));
        candleEntityList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

        while (candleMap.size() > MAX_STORAGE_CANDLE_COUNT) {
            candleMap.pollLastEntry();
        }

        allSymbolMap.put(symbol, candleMap);
        log.info("DB 로드 - {}:{}", symbol, candleEntityList.size());
    }
}
