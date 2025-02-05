package com.jh.coincoin.service.slack.interactive.sheet;


import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderSheet implements SheetHandler {

    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;

    @Autowired
    public void setOrderStrategyMap(Set<OrderStrategy> orderStrategySet) {
        this.orderStrategyMap = orderStrategySet.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
    }

    @Override
    public SheetType getType() {
        return SheetType.ORDER;
    }

    @Override
    public void updateSheet(JsonNode jsonNode) {

    }

    @Override
    public void submitSheet(JsonNode jsonNode) {

    }
}
