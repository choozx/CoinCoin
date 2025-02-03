package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;

public interface RiskRewardCalculator {

    RiskRewardRatioType getType();
    double calcPrice(Side side, Order order, double entryPrice, double ratio);
}
