package com.jh.coincoin.service.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.BinanceType.EventType;

/**
 * Created by dale on 2025-02-07.
 */
public interface EventHandler {

    EventType getType();
    void process(JsonNode jsonNode);
}
