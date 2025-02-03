package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PeakRatio implements RiskRewardCalculator {

    @Override
    public RiskRewardRatioType getType() {
        return RiskRewardRatioType.PEAK_RATIO;
    }

    @Override
    public double calcPrice(Side side, Order order, double entryPrice, double ratio) {
        double prePeakPrice = 0;   // PEAK_RATIO는 전 분봉의 저점, 고점이 필요함. 구하려면 symbol, interval이 필요한데...
        if (order.equals(Order.STOP_MARKET))
            return prePeakPrice;

        double priceGap = Math.abs(entryPrice - prePeakPrice);
        return side.equals(Side.BUY) ? entryPrice + (priceGap * ratio) : entryPrice - (priceGap * ratio);
    }
}
