package com.jh.coincoin.service.websocket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.Binance.TradeLogDto;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.EventType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.TradeLogService;
import com.jh.coincoin.service.strategy.buy.BuyStrategy;
import com.jh.coincoin.service.websocket.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2025-02-07.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTradeUpdate implements EventHandler {

    private final TradeLogService tradeLogService;

    private Map<BuyStrategyType, BuyStrategy> buyStrategyMap;

    @Autowired
    public void setBuyStrategyMap(Set<BuyStrategy> buyStrategySet) {
        this.buyStrategyMap = buyStrategySet.stream().collect(Collectors.toMap(BuyStrategy::getType, Function.identity()));
    }

    @Override
    public EventType getType() {
        return EventType.ORDER_TRADE_UPDATE;
    }

    @Override
    @Transactional
    public void process(JsonNode jsonNode) {
        log.warn("바이낸스 OrderUpdateJson : {}", jsonNode);
        JsonNode objectJsonNode = jsonNode.path("o");
        Symbol symbol = Symbol.of(objectJsonNode.path("s").asText());

        OrderState orderState = OrderState.valueOf(objectJsonNode.path("X").asText());

        if (orderState.equals(OrderState.FILLED)) {
            TradeLogDto activePosition = tradeLogService.getActivePosition(symbol);
            BuyStrategy buyStrategy = buyStrategyMap.get(activePosition.getBuyStrategyType());

            buyStrategy.afterFilled(objectJsonNode);
        }
    }
}
