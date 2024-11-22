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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-09-11.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RSIIndicator implements Indicator {

    private final CandleService candleService;
    private final AdminService adminService;

    @Override
    public IndicatorType getName() {
        return IndicatorType.RSI;
    }

    @Override
    public Double getLastFigure(Symbol symbol, Interval interval) {
        Map<Long, Candle> candleMap = candleService.getCandleListPerInterval(symbol, interval, 200);

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
        Pair<Double, Double> rsiSettingPair = adminService.getRsiSetting();
        return result <= rsiSettingPair.getLeft() || result >= rsiSettingPair.getRight();
    }

}
