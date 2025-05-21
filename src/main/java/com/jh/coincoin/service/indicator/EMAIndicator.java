package com.jh.coincoin.service.indicator;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.repo.jdbc.IndicatorBatchRepository;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by dale on 2024-09-11.
 * EMA는 Period가 유동적이라 모두 저장하기에는 너무 많음.
 * 따라서, 그때그때 계산하는 방식으로 하자
 */

@Slf4j
@Service
public class EMAIndicator extends Indicator {


    public EMAIndicator(CandleService candleService, IndicatorBatchRepository indicatorBatchRepository) {
        super(candleService, indicatorBatchRepository);
    }

    private static final int CANDLE_COUNT = 200;

    @Override
    public IndicatorType getType() {
        return IndicatorType.EMA;
    }

    @Override
    public TextObject wrappingMessage(Symbol symbol, Interval interval) {
        return MarkdownTextObject.builder()
                .text(String.format("*[%s]* : %.2f", symbol, 0d))
                .build();
    }

    @Override
    public boolean isDetectLastValue(Symbol symbol, Interval interval) {
        return false;
    }

    @Override
    public void update(Symbol symbol, Interval interval) {

    }

    public TreeMap<Long, Double> getValueMap(Symbol symbol, Interval interval, long begin, long end, int period) {
        long beginTime = adjustBeginTime(begin, interval); // rsi값을 구하기 위해서는 200개의 캔들이 필요
        TreeMap<Long, Candle> candleMap = candleService.getCandleMapToDB(symbol, interval, beginTime, end);

        Deque<Candle> deque = new ArrayDeque<>();
        TreeMap<Long, Double> emaMap = new TreeMap<>();
        for (var entry : candleMap.entrySet()) {
            deque.offer(entry.getValue());

            if (deque.size() != CANDLE_COUNT)
                continue;

            long openTime = entry.getKey();
            double ema = formula(deque, period);

            emaMap.put(openTime, ema);

            deque.poll();
        }

        return emaMap;
    }

    private long adjustBeginTime(long begin, Interval interval) {
        LocalDateTime nextOpenTime = DateTimeUtil.toDateTime(begin).plusMinutes(interval.getMinute());
        return DateTimeUtil.toEpochMilli(nextOpenTime.minusMinutes((long) interval.getMinute() * CANDLE_COUNT)); // rsi값을 구하기 위해서는 200개의 캔들이 필요
    }

    public static Double formula(Deque<Candle> prices, int period) {
        if (prices == null || prices.size() < period) {
            return null; // 데이터 부족
        }

        double multiplier = 2.0 / (period + 1);
        Double ema = null;

        Iterator<Candle> iterator = prices.iterator();
        int index = 0;
        double[] buffer = new double[period];

        while (iterator.hasNext()) {
            double price = iterator.next().getClosePrice();

            if (index < period) {
                buffer[index] = price;
                if (index == period - 1) {
                    double sum = 0.0;
                    for (double p : buffer) sum += p;
                    ema = sum / period; // 초기 EMA
                }
            } else {
                ema = (price - ema) * multiplier + ema;
            }

            index++;
        }

        return ema;
    }
}
