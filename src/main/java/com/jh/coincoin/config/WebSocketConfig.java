package com.jh.coincoin.config;

import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import com.jh.coincoin.service.ListenerService;
import com.jh.coincoin.service.external.BinanceAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@DependsOn("restClient")
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final BinanceAPIService binanceAPIService;
    private final ListenerService listenerService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String listenKey = binanceAPIService.getListenKey();
        String webSocketUrl = BinanceURL.WEB_SOCKET_BASE_URL.getUrl() + listenKey;

        registry.addHandler(listenerService, webSocketUrl).setAllowedOrigins("*");
    }
}
