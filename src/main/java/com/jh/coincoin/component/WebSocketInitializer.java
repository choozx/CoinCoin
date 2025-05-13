//package com.jh.coincoin.component;
//
//import com.jh.coincoin.model.Binance;
//import com.jh.coincoin.model.type.BinanceType;
//import com.jh.coincoin.service.ListenerService;
//import com.jh.coincoin.service.external.BinanceAPIService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.client.WebSocketClient;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//
//import java.util.concurrent.CompletableFuture;
//
///**
// * Created by dale on 2025-02-07.
// */
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class WebSocketInitializer {
//
//    private final ListenerService listenerService;
//    private final BinanceAPIService binanceAPIService;
//
//    @PostConstruct
//    public void connect() {
//        Binance.ListenKeyRes res = binanceAPIService.getListenKey();
//        String webSocketUrl = BinanceType.BinanceURL.WEB_SOCKET_BASE_URL.getUrl() + res.getListenKey();
//
//        WebSocketClient client = new StandardWebSocketClient();
//        CompletableFuture<WebSocketSession> futureSession = client.execute(listenerService, webSocketUrl);
//
//        futureSession.thenAccept(webSocketSession -> {
//            log.info("✅ Binance WebSocket 연결 성공!");
//        }).exceptionally(ex -> {
//            log.info("❌ WebSocket 연결 실패: {}", ex.getMessage());
//            return null;
//        });
//    }
//}
