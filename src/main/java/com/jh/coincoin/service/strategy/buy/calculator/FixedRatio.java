package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import org.springframework.stereotype.Service;

@Service
public class FixedRatio implements RiskRewardCalculator{

    @Override
    public RiskRewardRatioType getType() {
        return RiskRewardRatioType.FIXED_RATIO;
    }

    @Override
    public double calcPrice(Side side, Order order, double entryPrice, double ratio) {
        double priceChange = entryPrice * ratio / 100;
        return entryPrice + (order == Order.TAKE_PROFIT_MARKET ? priceChange : -priceChange) * (side == Side.BUY ? 1 : -1);
    }

}
