package com.jh.coincoin.service;

import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.repo.CandleRepository;
import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.DateTimeUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private Map<Symbol, TreeMap<Long, Candle>> allSymbolMap;

    @PostConstruct
    public void init() {
        allSymbolMap = new TreeMap<>();
        allSymbolLoad2DB();
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval) {
        var candleMap = allSymbolMap.get(symbol);
        return getCandleMap(symbol, interval, candleMap.firstKey(), candleMap.lastKey());
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval, long beginTime) {
        return getCandleMap(symbol, interval, beginTime, DateTimeUtil.getCurrentTimeMillis());
    }

    public TreeMap<Long, Candle> getCandleMap(Symbol symbol, Interval interval, long beginTime, long endTime) {
        TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);
        beginTime = Math.max(beginTime, candleMap.firstKey());

        return mergeCandle(candleMap, interval, beginTime, endTime);
    }

    public TreeMap<Long, Candle> getCandleMapByBeginToDB(Symbol symbol, Interval interval, long beginTime, int candleCount) {
        long endTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(beginTime).plusMinutes(((long) candleCount *interval.getMinute()) + interval.getMinute()));
        TreeMap<Long, Candle> loadCandleMap = load2DBV2(symbol, beginTime, endTime);

        return mergeCandle(loadCandleMap, interval, beginTime, endTime);
    }

    public TreeMap<Long, Candle> getCandleMapToDB(Symbol symbol, Interval interval, long beginTime, long end) {
        TreeMap<Long, Candle> loadCandleMap = load2DBV2(symbol, beginTime, end);

        return mergeCandle(loadCandleMap, interval, beginTime, end);
    }

    public Candle getLastCandle(Symbol symbol, Interval interval) {
        TreeMap<Long, Candle> lastCandleMap = getCandleMap(symbol, interval);
        log.warn("[{}] | lastKey:{} firstKey:{}", symbol, DateTimeUtil.toDateTime(lastCandleMap.lastKey()), DateTimeUtil.toDateTime(lastCandleMap.firstKey()));
        return lastCandleMap.lastEntry().getValue();
    }

    public Candle getLastCandleToDB(Symbol symbol, Interval interval) {
        int minutes = interval.getMinute();
        long end = DateTimeUtil.floorToInterval(DateTimeUtil.getCurrentTimeMillis(), minutes);
        long begin = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(end).minusMinutes(minutes));

        log.warn("[{}] | 캔들 시작 시간:{} 캔들 종료 시간:{}", symbol, DateTimeUtil.toDateTime(begin), DateTimeUtil.toDateTime(end));
        List<CandleEntity> candleList = candleRepository.findAllBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(symbol, begin, end);
        TreeMap<Long, Candle> candleMap = new TreeMap<>();
        candleList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

        return mergeCandle(candleMap, interval, begin, end).lastEntry().getValue();
    }

    public void update() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        long now = DateTimeUtil.getCurrentTimeMillis();
        for (Symbol symbol : symbolList) {
            TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);
            if (candleMap == null)
                throw new ServerException(ErrorType.COMMON_FAIL, String.format("캔들을 찾을 수 없습니다! %s", symbol));

            long lastUpdateTime = candleMap.lastKey();
            long nextLoadCandleTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(lastUpdateTime).plusMinutes(Interval.ONE_MINUTE.getMinute()));

            TreeMap<Long, Candle> loadCandleMap = load2DBV2(symbol, nextLoadCandleTime, now);
            candleMap.putAll(loadCandleMap);

            log.info("{} 마지막 시간 : {}", symbol, DateTimeUtil.toDateTime(loadCandleMap.lastKey()));
        }

        log.info("캔들 로드 완료");
    }

    public void removeTrackingCandle(Symbol symbol) {
        allSymbolMap.remove(symbol);
    }

    private TreeMap<Long, Candle> mergeCandle(TreeMap<Long, Candle> map, Interval interval, long beginTime, long endTime) {
        long roundBeginTime = DateTimeUtil.ceilToInterval(beginTime, interval.getMinute());
        long floorEndTime = DateTimeUtil.floorToInterval(endTime, interval.getMinute());
        var subCandleMap = new TreeMap<>(map.subMap(roundBeginTime, floorEndTime));

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

    private void allSymbolLoad2DB() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        long now = DateTimeUtil.getCurrentTimeMillis();
        LocalDateTime targetTime = DateTimeUtil.toDateTime(now).minusMinutes(MAX_STORAGE_CANDLE_COUNT+1);
        long beginTime = DateTimeUtil.toEpochMilli(targetTime);
        for (Symbol symbol : symbolList) {
            TreeMap<Long, Candle> candleMap = load2DBV2(symbol, beginTime, now);

            log.info("{} 마지막 시간 : {}", symbol, DateTimeUtil.toDateTime(candleMap.lastKey()));
            allSymbolMap.put(symbol, candleMap);
        }
    }

    private TreeMap<Long, Candle> load2DBV2(Symbol symbol, long beginTime, long endTime) {
        long ceil = DateTimeUtil.ceilToInterval(beginTime, Interval.ONE_MINUTE.getMinute());
        long floor = DateTimeUtil.floorToInterval(endTime, Interval.ONE_MINUTE.getMinute());
        List<CandleEntity> candleEntityList = candleRepository.findAllByOpenTimeBetweenAndSymbol(ceil, floor, symbol);
        TreeMap<Long, Candle> candleMap = new TreeMap<>();
        candleEntityList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

        log.info("DB 로드 - {}:{}", symbol, candleMap.size());
        return candleMap;
    }
}
