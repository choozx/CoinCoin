package com.jh.coincoin.service.strategy.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.slack.api.model.block.InputBlock;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by dale on 2024-11-22.
 * 이 클레스는 어떤 조건으로 포지션을 잡을지에 대한 클레스이다.
 */
public interface OrderStrategy {

    OrderStrategyType getType();

    List<InputBlock> getInputBlockList();

    Pair<Boolean, Side> isHit(Symbol symbol, Interval interval, String targetValue);

    void save(JsonNode decideStrategy);
}
