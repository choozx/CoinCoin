package com.jh.coincoin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Binance.Event;
import com.jh.coincoin.model.type.BinanceType.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


/**
 * Created by dale on 2025-02-06.
 */

@Slf4j
@Service
public class ListenerService extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("message: {}", message.getPayload());

        Event event;
        try {
            event = objectMapper.readValue(message.getPayload(), Event.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        EventType type = EventType.of(event.getEventType());
        if (type == null)
            return;

        log.info("occur event : {}", type);
    }
}