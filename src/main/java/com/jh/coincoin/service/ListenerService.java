package com.jh.coincoin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Binance.Event;
import com.jh.coincoin.model.type.BinanceType.EventType;
import com.jh.coincoin.service.websocket.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by dale on 2025-02-06.
 */

@Slf4j
@Service
public class ListenerService extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<EventType, EventHandler> eventHandlerMap;

    @Autowired
    public void setEventHandlerMap(Set<EventHandler> eventHandlerSet) {
        this.eventHandlerMap = eventHandlerSet.stream().collect(Collectors.toMap(EventHandler::getType, Function.identity()));
    }

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

        EventHandler eventHandler = eventHandlerMap.get(type);
        eventHandler.process(event.getObjects());
    }
}