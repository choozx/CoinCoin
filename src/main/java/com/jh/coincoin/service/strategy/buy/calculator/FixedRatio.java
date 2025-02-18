package com.jh.coincoin.service.strategy.buy.calculator;

import com.jh.coincoin.model.Strategy.PriceCalculatorDto;
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
    public double calcPrice(PriceCalculatorDto priceDto) {
        double priceChange = priceDto.getEntryPrice() * priceDto.getRiskRewardRatio() / 100;
        return priceDto.getEntryPrice() + (priceDto.getOrder() == Order.TAKE_PROFIT_MARKET ? priceChange : -priceChange) * (priceDto.getSide() == Side.BUY ? 1 : -1);
    }

    @Override
    public double calcPriceForBackTest(PriceCalculatorDto priceDto, long entryTime) {
        return 0;
    }

}
