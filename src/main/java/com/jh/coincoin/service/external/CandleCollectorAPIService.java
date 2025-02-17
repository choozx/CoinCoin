package com.jh.coincoin.service.external;

import com.jh.coincoin.model.CandleCollector.CollectSymbolReq;
import com.jh.coincoin.model.CandleCollector.TrackingSymbolReq;
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
        TrackingSymbolReq req = new TrackingSymbolReq(symbol.getKey());

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

    public void startCollectPastCandle(Symbol symbol) {
        CollectSymbolReq req = new CollectSymbolReq(symbol.getKey());

        restClient.post()
                .uri(baseUrl + CollectorType.COLLECT_PAST_CANDLE.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.value() == 1002, (request, response) -> {
                    throw new ServerException(ErrorType.COMMON_FAIL, String.format("현재 캔들을 수집중 입니다. 잠시 후 다시 시도해주세요.")); // TODO : candle-collector 에서 현재 수집중인 심볼가져와야됨
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ServerException(ErrorType.COMMON_FAIL, String.format("%s 과거 캔들 수집 실패!", symbol.getKey()));
                })
                .toBodilessEntity();
    }
}
