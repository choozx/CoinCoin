package com.jh.coincoin.service.strategy.buy;

import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by dale on 2024-11-22.
 */

@Service
@RequiredArgsConstructor
public class StopAndLimitBuyStrategy implements BuyStrategy{

    @Override
    public BuyStrategyType getType() {
        return BuyStrategyType.STOP_AND_LIMIT;
    }

    @Override
    public void order() {

    }
}
