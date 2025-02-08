package com.jh.coincoin.service.websocket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.BinanceType.EventType;
import com.jh.coincoin.service.websocket.EventHandler;
import org.springframework.stereotype.Service;

/**
 * Created by dale on 2025-02-08.
 */
@Service
public class TradeLite implements EventHandler {
    @Override
    public EventType getType() {
        return EventType.TRADE_LITE;
    }

    @Override
    public void process(JsonNode jsonNode) {

    }
}
