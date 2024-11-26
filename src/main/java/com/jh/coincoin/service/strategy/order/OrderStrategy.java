package com.jh.coincoin.service.strategy.order;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;

/**
 * Created by dale on 2024-11-22.
 */
public interface OrderStrategy {

    OrderStrategyType getType();

    boolean isHit(BinanceType.Symbol symbol);
}
