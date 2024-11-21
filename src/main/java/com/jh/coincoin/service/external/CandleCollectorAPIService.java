package com.jh.coincoin.service.external;

import com.jh.coincoin.model.CandleCollector;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.CollectorType;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Created by dale on 2024-11-21.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleCollectorAPIService {

    @Value("${candle-collector.url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public void orderTrackingSymbol(Symbol symbol) {
        CandleCollector.TrackingSymbolReq req = new CandleCollector.TrackingSymbolReq(symbol.getKey());

        restClient.post()
                .uri(baseUrl + CollectorType.COLLECT_START.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ServerException(ErrorType.COMMON_FAIL, String.format("%s 등록 실패!", symbol.getKey()));
                })
                .toBodilessEntity();
    }
}
