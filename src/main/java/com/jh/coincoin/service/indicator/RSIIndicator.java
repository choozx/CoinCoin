package com.jh.coincoin.service.indicator;

import com.jh.coincoin.entity.IndicatorEntity;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.repo.IndicatorRepository;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.TextObject;
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
public class RSIIndicator extends Indicator {


    public RSIIndicator(CandleService candleService, IndicatorRepository indicatorRepository) {
        super(candleService, indicatorRepository);
    }

    record RSIKey(Symbol symbol, Interval interval) { }
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

        long end = DateTimeUtil.getCurrentTimeMillis();
        long begin = rsiValueMap.isEmpty() ? DateTimeUtil.calcBeginTime(end, interval.getMinute(), 1000) : rsiValueMap.lastKey();

        Map<Long, Double> newValueMap = getValueMap(symbol, interval, begin, end);
        rsiValueMap.putAll(newValueMap);

        while (rsiValueMap.size() > GlobalConst.MAX_STORAGE_INDICATOR_COUNT)
            rsiValueMap.pollFirstEntry();

        var lastEntry = rsiMap.get(rsiKey).lastEntry();
        log.info("RSI 업데이트 :: symbol:{}, time:{}, value:{}", symbol, DateTimeUtil.toDateTime(lastEntry.getKey()), lastEntry.getValue());
    }

    public TreeMap<Long, Double> getValueMap(Symbol symbol, Interval interval, long begin, long end) {
        Map<Long, IndicatorEntity> indicatorEntityMap = getIndicatorEntityMap(IndicatorType.RSI, symbol, interval, begin, end);

        long beginTime = adjustBeginTime(begin, interval); // rsi값을 구하기 위해서는 200개의 캔들이 필요
        // FIXME 아마 추후에는 getCandleMap으로 바꿔야함 ex) 처음은 redis에서 캔들 검색 -> 없으면 db에서 가져오기
        TreeMap<Long, Candle> candleMap = candleService.getCandleMapToDB(symbol, interval, beginTime, end);

        Deque<Candle> deque = new ArrayDeque<>();
        TreeMap<Long, Double> rsiMap = new TreeMap<>();
        for (var entry : candleMap.entrySet()) {
            deque.offer(entry.getValue());

            if (deque.size() != CANDLE_COUNT)
                continue;

            long openTime = entry.getKey();

            double rsi;
            if (indicatorEntityMap.containsKey(openTime)) {
                String[] rsiString = indicatorEntityMap.get(openTime).getValueArray();
                rsi = Double.parseDouble(rsiString[0]);
            } else {
                rsi = formula(deque);
                save(IndicatorType.RSI, symbol, interval, openTime, String.valueOf(rsi));
            }

            rsiMap.put(openTime, rsi);

            deque.poll();
        }

        return rsiMap;
    }

    public void changeRSIValue(double low, double high) {
        rsiValuePair = Pair.of(low, high);
    }

    public long adjustBeginTime(long begin, Interval interval) {
        LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(begin).plusMinutes(interval.getMinute());
        return DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * CANDLE_COUNT)); // rsi값을 구하기 위해서는 200개의 캔들이 필요
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
