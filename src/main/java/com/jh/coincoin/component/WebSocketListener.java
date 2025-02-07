package com.jh.coincoin.component;

import com.jh.coincoin.service.ListenerService;
import com.jh.coincoin.service.external.BinanceAPIService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketListener {

    private final BinanceAPIService binanceAPIService;
    private final ListenerService listenerService;
    private final StandardWebSocketClient client = new StandardWebSocketClient();

    @PostConstruct
    public void init() {
//        String listenKey = binanceAPIService.getListenKey();

        String wsUrl = "ws://localhost:8080/ws/test";

        // WebSocket 연결
        CompletableFuture<WebSocketSession> future = client.execute(listenerService, wsUrl);
        future.thenAccept(session -> {
            log.info("WebSocket 연결 성공!");
        }).exceptionally(ex -> {
            log.error("WebSocket 연결 실패: {}", ex.getMessage());
            return null;
        });
    }
}
