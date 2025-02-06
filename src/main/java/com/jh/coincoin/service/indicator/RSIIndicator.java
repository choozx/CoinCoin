package com.jh.coincoin.service.indicator;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by dale on 2024-09-11.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RSIIndicator implements Indicator {

    private final CandleService candleService;
    private final AdminService adminService;

    record RSIKey(Symbol symbol, Interval interval) {}

    Map<RSIKey, TreeMap<Long, Double>> rsiMap = new HashMap<>();
    private static final int CANDLE_COUNT = 200;

    @Override
    public IndicatorType getName() {
        return IndicatorType.RSI;
    }

    @Override
    public Double getLastFigure(Symbol symbol, Interval interval) {
        Map<Long, Candle> candleMap = candleService.getCandleMap(symbol, interval, CANDLE_COUNT);

        List<Candle> candles = candleMap.values().stream()
                .sorted(Comparator.comparing(Candle::getOpenTime))
                .toList();

        List<Double> upList = new ArrayList<>();
        List<Double> downList = new ArrayList<>();
        for (int i = 0; i < candles.size() - 1; i++) {
            double priceChange = candles.get(i + 1).getClosePrice() - candles.get(i).getClosePrice();
            if (priceChange > 0) {
                upList.add(priceChange);
                downList.add(0d);
            } else if (priceChange < 0){
                upList.add(0d);
                downList.add(Math.abs(priceChange));
            } else {
                upList.add(0d);
                downList.add(0d);
            }
        }

        int period = adminService.getRsiPeriod();
        double ema = (double) 1 / (1 + (period - 1));

        double au = upList.get(0);
        for (Double up : upList) {
            au = (up * ema) + (au * (1 - ema));
        }

        double ad = downList.get(0);
        for (Double down : downList) {
            ad = (down * ema) + (ad * (1 - ema));
        }

        double rs = au / ad;
        double rsi = 100 - (100 / (1 + rs));

        log.info("RSI :: {} -> {}", symbol, rsi);
        return rsi;
    }

    @Override
    public String wrappingMessage(Symbol symbol, Double result) {
        return symbol + " : " + String.format("%.2f", result);
    }

    @Override
    public boolean isDetect(Double result) {
        Pair<Double, Double> rsiValuePair = adminService.getAlertRsiValuePair();
        return result <= rsiValuePair.getLeft() || result >= rsiValuePair.getRight();
    }

    @Override
    public void update(List<Symbol> symbolList) {
        for (Symbol symbol : symbolList) {
            for (Interval interval : Interval.values()) {
                RSIKey rsiKey = new RSIKey(symbol, interval);
                TreeMap<Long, Double> rsiValueMap = rsiMap.getOrDefault(rsiKey, new TreeMap<>(Comparator.reverseOrder()));

                long lastOpenTime = 0;
                if (!rsiValueMap.isEmpty()) {
                    lastOpenTime = rsiValueMap.firstKey();
                }

                Map<Long, Candle> candleMap = candleService.getCandleMap(symbol, interval, lastOpenTime);
            }
        }
    }
}
