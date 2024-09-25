package com.jh.coincoin.service;

import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.repo.CandleRepository;
import com.jh.coincoin.util.DateTimeUtil;
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

import static com.jh.coincoin.model.consts.GlobalConst.*;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@Service
public class CandleService {

    private final BinanceFutureAPIService binanceFutureAPIService;
    private final AdminService adminService;
    private final CandleRepository candleRepository;

    private Map<Symbol, TreeMap<Long, Candle>> allSymbolMap;

    public CandleService(BinanceFutureAPIService binanceFutureAPIService, AdminService adminService, CandleRepository candleRepository) {
        this.binanceFutureAPIService = binanceFutureAPIService;
        this.adminService = adminService;
        this.candleRepository = candleRepository;

        this.allSymbolMap = new HashMap<>();

        load2DB();
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

    public void update(){
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        long now = DateTimeUtil.getCurrentTimeMillis();
        long endTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(now).truncatedTo(ChronoUnit.MINUTES).minusMinutes(1));
        List<Candle> newCandleList = new ArrayList<>();
        for (Symbol symbol : symbolList) {
            TreeMap<Long, Candle> candleMap = allSymbolMap.get(symbol);

            long startSeedTime = candleMap.isEmpty() ? DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(now).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT)) : candleMap.firstKey();
            long startTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(startSeedTime).plusMinutes(1));

            log.info("now : {} | start:{} | end:{}", DateTimeUtil.toDateTime(now), DateTimeUtil.toDateTime(startTime), DateTimeUtil.toDateTime(endTime));
            List<Candle> candleList = externalUpdate(symbol, startTime, endTime);
            log.info("API 로드 - {}:{}", symbol, candleList.size());

            newCandleList.addAll(candleList);
        }

        saveCandleList(newCandleList);

        log.info("캔들 로드 완료");
    }

    private void load2DB() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        LocalDateTime targetTime = DateTimeUtil.toDateTime(DateTimeUtil.getCurrentTimeMillis()).truncatedTo(ChronoUnit.MINUTES).minusMinutes(MAX_STORAGE_CANDLE_COUNT);
        long targetTimestamp = DateTimeUtil.toEpochMilli(targetTime);
        for (Symbol symbol : symbolList) {
            List<CandleEntity> candleEntityList = candleRepository.findTop100ByOpenTimeAfterAndSymbol(targetTimestamp, symbol);
            TreeMap<Long, Candle> candleMap = new TreeMap<>(Comparator.reverseOrder());
            candleEntityList.forEach(entity -> candleMap.put(entity.getOpenTime(), new Candle(entity)));

            allSymbolMap.put(symbol, candleMap);
            log.info("DB 로드 - {}:{}", symbol, candleMap.size());
        }
    }

    private List<Candle> externalUpdate(Symbol symbol, long startTime, long endTime){
        List<List<Object>> rawList = binanceFutureAPIService.getCandleList(symbol.getKey(), Interval.ONE_MINUTE.getName(), startTime, endTime);

        if (rawList ==  null)
            return new ArrayList<>();

        Map<Long, Candle> candleMap = allSymbolMap.get(symbol);
        List<Candle> newCandleList = new ArrayList<>();
        for (var rawCandle: rawList) {
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
