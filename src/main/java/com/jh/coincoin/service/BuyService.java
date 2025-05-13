package com.jh.coincoin.service;

import com.jh.coincoin.model.Binance.BuyResultDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.Strategy.BuyParamDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.strategy.buy.BuyStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2025-05-12.
 */
@Service
@RequiredArgsConstructor
public class BuyService {

    private Map<BuyStrategyType, BuyStrategy> buyStrategyMap;

    @Autowired
    public void setBuyStrategyMap(Set<BuyStrategy> buyStrategySet) {
        this.buyStrategyMap = buyStrategySet.stream().collect(Collectors.toMap(BuyStrategy::getType, Function.identity()));
    }

    public BuyResultDto buyPosition(Side side, TradeStrategyDto tradeStrategyDto) {
        BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
        BuyStrategyType buyStrategyType = buyStrategyDto.getType();
        BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());

        BuyParamDto buyParamDto = BuyParamDto.builder()
                .symbol(tradeStrategyDto.getSymbol())
                .side(side)
                .interval(tradeStrategyDto.getInterval())
                .leverage(buyStrategyDto.getLeverage())
                .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                .targetValue(buyStrategyDto.getTargetValue())
                .build();

        BuyResultDto buyResultDto = buyStrategy.buy(buyParamDto);
        buyResultDto.setStrategy(buyStrategyType, tradeStrategyDto.getOrderStrategy().getType());

        return buyResultDto;
    }
}
