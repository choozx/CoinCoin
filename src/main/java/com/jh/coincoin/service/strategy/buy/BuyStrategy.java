package com.jh.coincoin.service.strategy.buy;

import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;

/**
 * Created by dale on 2024-11-22.
 */
public interface BuyStrategy {

    BuyStrategyType getType();

    void order();
}
