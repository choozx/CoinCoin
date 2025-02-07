package com.jh.coincoin.service.strategy.buy;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.Strategy.BuyParamDto;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.slack.api.model.block.InputBlock;

import java.util.List;

/**
 * Created by dale on 2024-11-22.
 * 이 클레스는 예산을 어떻게 분배할지에 대한 클레스이다
 */

public interface BuyStrategy {

    BuyStrategyType getType();
    PositionInfoRes order(BuyParamDto buyParamDto);
    List<InputBlock> getInputBlockList();

    BuyStrategyEntity newEntity(JsonNode decideStrategyValue);
}
