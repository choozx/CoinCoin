package com.jh.coincoin.service.websocket.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.BinanceType.EventType;
import com.jh.coincoin.service.websocket.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by dale on 2025-02-08.
 */
@Slf4j
@Service
public class TradeLite implements EventHandler {
    @Override
    public EventType getType() {
        return EventType.TRADE_LITE;
    }

    @Override
    public void process(JsonNode jsonNode) {
        log.warn("바이낸스 간략한 채결 정보 : {}", jsonNode);
    }
}
