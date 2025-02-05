package com.jh.coincoin.service.strategy.buy;

import com.jh.coincoin.model.Strategy.OrderParamDto;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.slack.api.model.block.InputBlock;

import java.util.List;

/**
 * Created by dale on 2024-11-22.
 * 이 클레스는 예산을 어떻게 분배할지에 대한 클레스이다
 */

public interface BuyStrategy {

    BuyStrategyType getType();
    void order(OrderParamDto orderParamDto);
    List<InputBlock> getInputBlockList();
}
