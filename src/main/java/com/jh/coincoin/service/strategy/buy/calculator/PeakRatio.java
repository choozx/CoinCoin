package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.Strategy.PriceCalculatorDto;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeakRatio implements RiskRewardCalculator {

    private final CandleService candleService;

    @Override
    public RiskRewardRatioType getType() {
        return RiskRewardRatioType.PEAK_RATIO;
    }

    @Override
    public double calcPrice(PriceCalculatorDto priceDto) {
        double entryPrice = priceDto.getEntryPrice();
        double ratio = priceDto.getRiskRewardRatio();

        Candle lastCandleToDB = candleService.getLastCandleToDB(priceDto.getSymbol(), priceDto.getInterval());

        double prePeakPrice = priceDto.getSide().equals(Side.BUY) ? lastCandleToDB.getLowPrice() : lastCandleToDB.getHighPrice();

        double priceGap = Math.abs(entryPrice - prePeakPrice);
        double minGap = entryPrice * GlobalConst.MIN_ORDER_PRICE_GAP_PER; // 0.1%의 차이

        priceGap = Math.max(priceGap, minGap);
        if (priceDto.getOrder().equals(Order.STOP_MARKET)) {
            return priceDto.getSide().equals(Side.BUY) ? entryPrice - priceGap : entryPrice + priceGap;
        }

        Candle lastCandleToServer = candleService.getLastCandle(priceDto.getSymbol(), priceDto.getInterval());
        log.warn(String.format("[%s] 캔들 시간 : %s | 캔들 정보: %s", priceDto.getSymbol(), DateTimeUtil.toDateTime(lastCandleToServer.getOpenTime()), lastCandleToServer));

        return priceDto.getSide().equals(Side.BUY) ? entryPrice + (priceGap * ratio) : entryPrice - (priceGap * ratio);
    }

    @Override
    public double calcPriceForBackTest(PriceCalculatorDto priceDto, long entryTime) {
        Interval interval = priceDto.getInterval();
        long preEntryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(entryTime).minusMinutes(interval.getMinute()));
        double entryPrice = priceDto.getEntryPrice();
        double ratio = priceDto.getRiskRewardRatio();

        TreeMap<Long, Candle> candleMap = candleService.getCandleMapByBeginToDB(priceDto.getSymbol(), priceDto.getInterval(), preEntryTime, 1);
        Candle candle = candleMap.firstEntry().getValue();
        double prePeakPrice = priceDto.getSide().equals(Side.BUY) ? candle.getLowPrice() : candle.getHighPrice();

        double priceGap = Math.abs(entryPrice - prePeakPrice);
        double minGap = entryPrice * GlobalConst.MIN_ORDER_PRICE_GAP_PER; // 0.1%의 차이

        priceGap = Math.max(priceGap, minGap);
        if (priceDto.getOrder().equals(Order.STOP_MARKET)) {
            return priceDto.getSide().equals(Side.BUY) ? entryPrice - priceGap : entryPrice + priceGap;
        }


        return priceDto.getSide().equals(Side.BUY) ? entryPrice + (priceGap * ratio) : entryPrice - (priceGap * ratio);
    }
}
