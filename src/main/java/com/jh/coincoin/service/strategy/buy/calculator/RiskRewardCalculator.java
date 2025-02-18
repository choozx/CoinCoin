package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.Strategy.PriceCalculatorDto;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;

public interface RiskRewardCalculator {

    RiskRewardRatioType getType();
    double calcPrice(PriceCalculatorDto priceDto);
    double calcPriceForBackTest(PriceCalculatorDto priceDto, long entryTime);
}
