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
import java.util.ArrayList;
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

    private final BinanceFutureAPIService binanceFutureAPIService;
    private final AdminService adminService;
    private final CandleRepository candleRepository;

    private Map<Symbol, TreeMap<Long, Candle>> allSymbolMap = new HashMap<>();

    @PostConstruct
    public void init() {
        allSymbolLoad2DB();
        update();
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
        long endTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(now).truncatedTo(ChronoUnit.MINUTES).minusMinutes(1));
        List<Candle> allNewCandleList = new ArrayList<>();
        Map<Symbol, Integer> logMap = new HashMap<>();
        for (Symbol symbol : symbolList) {
            TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);

            long startSeedTime = candleMap.isEmpty() ? DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(now).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT)) : candleMap.firstKey();
            long startTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(startSeedTime).plusMinutes(1));

            log.info("now : {} | start:{} | end:{}", DateTimeUtil.toDateTime(now), DateTimeUtil.toDateTime(startTime), DateTimeUtil.toDateTime(endTime));
            List<Candle> newCandleList = externalUpdate(symbol, startTime, endTime);
            for (Candle candle : newCandleList) {
                candleMap.put(candle.getOpenTime(), candle);
            }

            if (candleMap.size() > MAX_STORAGE_CANDLE_COUNT) {
                int overflowCount = candleMap.size() - MAX_STORAGE_CANDLE_COUNT;
                for (int i = 0; i < overflowCount; i++) {
                    candleMap.pollLastEntry();
                }
            }
            logMap.put(symbol, newCandleList.size());

            allNewCandleList.addAll(newCandleList);
        }

        saveCandleList(allNewCandleList);

        logMap.forEach((symbol, size) -> log.info("{} : {}개 로드", symbol, size));
        log.info("캔들 로드 완료");
    }

    public void SymbolLoad2DB(Symbol symbol) {
        LocalDateTime targetTime = DateTimeUtil.toDateTime(DateTimeUtil.getCurrentTimeMillis()).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT);
        long targetTimestamp = DateTimeUtil.toEpochMilli(targetTime);

        load2DB(symbol, targetTimestamp);
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
        List<CandleEntity> candleEntityList = candleRepository.findTop100ByOpenTimeAfterAndSymbol(targetTimestamp, symbol);
        TreeMap<Long, Candle> candleMap = new TreeMap<>(Comparator.reverseOrder());
        candleEntityList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

        allSymbolMap.put(symbol, candleMap);
        log.info("DB 로드 - {}:{}", symbol, candleMap.size());
    }

    private List<Candle> externalUpdate(Symbol symbol, long startTime, long endTime) {
        // FIXME 서버를 끄고 다시 바로 시작하면 시작시간이 종료시간보다 앞서는 버그 있음
        List<List<Object>> rawList = binanceFutureAPIService.getCandleList(symbol.getKey(), Interval.ONE_MINUTE.getName(), startTime, endTime);

        if (rawList == null)
            return new ArrayList<>();

        Map<Long, Candle> candleMap = allSymbolMap.get(symbol);
        List<Candle> newCandleList = new ArrayList<>();
        for (var rawCandle : rawList) {
            Candle candle = new Candle(symbol, rawCandle);
            candleMap.put(candle.getOpenTime(), candle);
            newCandleList.add(candle);
        }

        return newCandleList;
    }

    private void saveCandleList(List<Candle> candleList) {
        List<CandleEntity> candleEntityList = new ArrayList<>();
        for (var candle : candleList) {
            CandleEntity e = CandleEntity.create(candle);
            candleEntityList.add(e);
        }

        candleRepository.saveAllAndFlush(candleEntityList);
    }
}
