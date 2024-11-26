package com.jh.coincoin.service.strategy.order;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jh.coincoin.model.type.StrategyType.OrderStrategyType.OVER_BOUGHT;

/**
 * Created by dale on 2024-11-22.
 */

@Service
@RequiredArgsConstructor
public class OverBoughtOrderStrategy implements OrderStrategy {

    @Override
    public OrderStrategyType getType() {
        return OVER_BOUGHT;
    }

    @Override
    public boolean isHit(Symbol symbol) {
        return false;
    }
}
