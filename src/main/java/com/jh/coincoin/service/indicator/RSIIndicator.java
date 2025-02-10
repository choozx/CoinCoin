package com.jh.coincoin.service.indicator;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.TextObject;
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
    public Double getLastValue(Symbol symbol, Interval interval) {
        RSIKey rsiKey = new RSIKey(symbol, interval);

        TreeMap<Long, Double> map = rsiMap.get(rsiKey);
        return map.lastEntry().getValue();
    }

    @Override
    public TextObject wrappingMessage(Symbol symbol, Double result) {
        return MarkdownTextObject.builder()
                .text(String.format("*[%s]* : %.2f", symbol, result))
                .build();
    }

    @Override
    public boolean isDetectLastValue(Double result) {
        return result <= rsiValuePair.getLeft() || result >= rsiValuePair.getRight();
    }

    @Override
    public void update(Symbol symbol, Interval interval) {
        RSIKey rsiKey = new RSIKey(symbol, interval);
        TreeMap<Long, Double> rsiValueMap = rsiMap.computeIfAbsent(rsiKey, k -> new TreeMap<>());

        TreeMap<Long, Candle> candleMap;
        long endTime = DateTimeUtil.getCurrentTimeMillis();
        if (rsiValueMap.isEmpty()) {
            candleMap = candleService.getCandleMap(symbol, interval, 0,endTime);
        } else {
            long lastOpenTime = rsiValueMap.firstKey();
            LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(lastOpenTime).plusMinutes(interval.getMinute());
            long beginTime = DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * CANDLE_COUNT)); // rsi값을 구하기 위해서는 200개의 캔들이 필요

            candleMap = candleService.getCandleMap(symbol, interval, beginTime, endTime);
        }

        Deque<Candle> deque = new ArrayDeque<>();
        for (var entry : candleMap.entrySet()) {
            deque.offer(entry.getValue());

            if (deque.size() != 200)
                continue;

            double rsi = formula(deque);
            rsiValueMap.put(entry.getKey(), rsi);

            if (rsiValueMap.size() > GlobalConst.MAX_STORAGE_INDICATOR_COUNT)
                rsiValueMap.pollFirstEntry();

            deque.poll();
        }

        var lastEntry = rsiMap.get(rsiKey).lastEntry();
        log.info("RSI 업데이트 :: symbol:{}, time:{}, value:{}", symbol, DateTimeUtil.toDateTime(lastEntry.getKey()), lastEntry.getValue());
    }

    public List<Pair<Long, Double>> getValueList(Symbol symbol, Interval interval, long begin, long end) {
        // TODO candle update 및 지표값 update
        return List.of();
    }

    public void changeRSIValue(double low, double high) {
        rsiValuePair = Pair.of(low, high);
    }

    private double formula(Deque<Candle> closePriceList) {
        List<Double> upList = new ArrayList<>();
        List<Double> downList = new ArrayList<>();

        // Deque의 요소를 Iterator를 이용해 순차적으로 접근
        Iterator<Candle> iterator = closePriceList.iterator();
        Candle previousCandle = iterator.next();  // 첫 번째 캔들
        while (iterator.hasNext()) {
            Candle currentCandle = iterator.next();
            double priceChange = currentCandle.getClosePrice() - previousCandle.getClosePrice();
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
            previousCandle = currentCandle; // 이전 캔들을 갱신
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
