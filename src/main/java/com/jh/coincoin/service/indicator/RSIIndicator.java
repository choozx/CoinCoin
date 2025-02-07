package com.jh.coincoin.service.indicator;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by dale on 2024-09-11.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RSIIndicator implements Indicator {

    record RSIKey(Symbol symbol, Interval interval) { }

    private final CandleService candleService;
    Map<RSIKey, TreeMap<Long, Double>> rsiMap = new HashMap<>();

    private Pair<Double, Double> rsiValuePair = Pair.of(30d, 70d);
    private static final int CANDLE_COUNT = 200;
    private static final int PERIOD = 14;

    @Override
    public IndicatorType getType() {
        return IndicatorType.RSI;
    }

    @Override
    public Double getLastFigure(Symbol symbol, Interval interval) {
        RSIKey rsiKey = new RSIKey(symbol, interval);

        TreeMap<Long, Double> map = rsiMap.get(rsiKey);
        return map.firstEntry().getValue();
    }

    @Override
    public String wrappingMessage(Symbol symbol, Double result) {
        return String.format("[%s] : %.2f", symbol, result);
    }

    @Override
    public boolean isDetect(Double result) {
        return result <= rsiValuePair.getLeft() || result >= rsiValuePair.getRight();
    }

    @Override
    public void update(Symbol symbol, Interval interval) {
        RSIKey rsiKey = new RSIKey(symbol, interval);
        TreeMap<Long, Double> rsiValueMap = rsiMap.computeIfAbsent(rsiKey, k -> new TreeMap<>(Comparator.reverseOrder()));


        TreeMap<Long, Candle> candleMap;
        if (rsiValueMap.isEmpty()) {
            candleMap = candleService.getCandleMap(symbol, interval);
        } else {
            long lastOpenTime = rsiValueMap.firstKey();
            LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(lastOpenTime).plusMinutes(interval.getMinute());
            long targetTime = DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * CANDLE_COUNT)); // rsi값을 구하기 위해서는 200개의 캔들이 필요

            candleMap = candleService.getCandleMap(symbol, interval, targetTime);

            while (candleMap.size() >= 50000)
                candleMap.pollLastEntry();
        }

        for (var candleEntry : candleMap.entrySet()) {
            List<Double> closePriceList = candleMap.tailMap(candleEntry.getKey()).values().stream().map(Candle::getClosePrice).limit(CANDLE_COUNT).toList();
            if (closePriceList.size() < CANDLE_COUNT)
                break;

            double rsi = formula(closePriceList);
            rsiValueMap.put(candleEntry.getKey(), rsi);
        }
    }

    public void changeRSIValue(double low, double high) {
        rsiValuePair = Pair.of(low, high);
    }

    private double formula(List<Double> closePriceList) {
        List<Double> upList = new ArrayList<>();
        List<Double> downList = new ArrayList<>();
        for (int i = 0; i < closePriceList.size() - 1; i++) {
            double priceChange = closePriceList.get(i + 1) - closePriceList.get(i);
            if (priceChange > 0) {
                upList.add(priceChange);
                downList.add(0d);
            } else if (priceChange < 0) {
                upList.add(0d);
                downList.add(Math.abs(priceChange));
            } else {
                upList.add(0d);
                downList.add(0d);
            }
        }

        double ema = (double) 1 / (1 + (PERIOD - 1));

        double au = upList.get(0);
        for (Double up : upList) {
            au = (up * ema) + (au * (1 - ema));
        }

        double ad = downList.get(0);
        for (Double down : downList) {
            ad = (down * ema) + (ad * (1 - ema));
        }

        double rs = au / ad;

        return 100 - (100 / (1 + rs));
    }
}
