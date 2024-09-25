package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Created by dale on 2024-09-11.
 */

@Service
@RequiredArgsConstructor
public class BinanceFutureAPIService {

    private final RestClient restClient;

    public List<List<Object>> getCandleList(String symbol, String interval, long startTime, long endTime){
        List<List<Object>> rawList = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_CANDLE.getUrl())
                        .queryParam("symbol",symbol)
                        .queryParam("interval", interval)
                        .queryParam("startTime", String.valueOf(startTime))
                        .queryParam("endTime", String.valueOf(endTime))
                        .build())
                .retrieve()
                .body(List.class);

        return rawList;
    }
}
