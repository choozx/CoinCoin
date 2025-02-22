package com.jh.coincoin.service.external;

import com.jh.coincoin.model.Binance.LeverageBracketReq;
import com.jh.coincoin.model.Binance.CancelOpenOrderReq;
import com.jh.coincoin.model.Binance.ListenKeyRes;
import com.jh.coincoin.model.Binance.ModifyLeverageReq;
import com.jh.coincoin.model.Binance.PositionInfoReq;
import com.jh.coincoin.model.Binance.CheckOrderReq;
import com.jh.coincoin.model.Binance.TickerPriceRes;
import com.jh.coincoin.model.Binance.TickerPriceReq;
import com.jh.coincoin.model.Binance.AccountBalanceRes;
import com.jh.coincoin.model.Binance.AccountBalanceReq;
import com.jh.coincoin.model.Binance.NewOrderRes;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-09-11.
 */

// 아마 이 클레스는 추후 주문 api등을 활용할듯
@Service
@Slf4j
@RequiredArgsConstructor
public class BinanceAPIService {

    @Value("${binance.api-key}")
    private final String apiKey;
    @Value("${binance.secret-key}")
    private final String secretKey;
    private final RestClient restClient;

    public List<PositionInfoRes> getPositionInfo(PositionInfoReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        List<Map<String, Object>> rawDataList = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_POSITION_INFO.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);

        if (rawDataList == null || rawDataList.size() == 0)
            return new ArrayList<>();

        List<PositionInfoRes> positionInfoList = new ArrayList<>();
        for (var rawData : rawDataList) {
            PositionInfoRes positionInfo = new PositionInfoRes(rawData);
            positionInfoList.add(positionInfo);
        }

        return positionInfoList;
    }

    public NewOrderRes checkOrder(CheckOrderReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        Map<String, Object> rawData = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_OPEN_ORDER.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

        if (rawData == null)
            throw new ServerException(ErrorType.COMMON_FAIL, "주문 오류!");

        return new NewOrderRes(rawData);
    }

    public List<NewOrderRes> getAllOrder(CheckOrderReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        List<Map<String, Object>> rawDataList = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_ALL_ORDER.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);

        if (rawDataList == null)
            throw new ServerException(ErrorType.COMMON_FAIL, "주문 오류!");

        List<NewOrderRes> newOrderResList = new ArrayList<>();
        for (var rawData : rawDataList) {
            newOrderResList.add(new NewOrderRes(rawData));
        }
        return newOrderResList;
    }

    public void modifyLeverage(ModifyLeverageReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.MODIFY_LEVERAGE.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);
    }

    public NewOrderRes newOrder(NewOrderReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        Map<String, Object> rawData = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.NEW_ORDER.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

        // TODO balance가 없을때 오류 처리

        if (rawData == null)
            throw new ServerException(ErrorType.COMMON_FAIL, "주문 오류!");

        return new NewOrderRes(rawData);
    }

    public void closeOpenOrder(CancelOpenOrderReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.CANCEL_ALL_ORDER.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .retrieve()
                .body(Map.class);

    }

    public void newTestOrder(NewOrderReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.NEW_TEST_ORDER.getUrl())
                        .query(queryString)
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
    }

    public AccountBalanceRes getAccountBalance(AccountBalanceReq req) {
        String queryString = req.toQueryString();
        String signature = makeSignature(queryString);

        List<Map<String, Object>> rawDataList = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_ACCOUNT_BALANCE.getUrl())
                        .query(req.toQueryString())
                        .queryParam("signature", signature)
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);

        AccountBalanceRes res = null;
        for (var rawData : rawDataList) {
            if (Objects.equals(rawData.get("asset"), "USDT"))
                res = new AccountBalanceRes(rawData);
        }

        return res;
    }

    public TickerPriceRes getTickerPrice(TickerPriceReq req) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.GET_TICKER_PRICE.getUrl())
                        .query(req.toQueryString())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TickerPriceRes.class);
    }

    public ListenKeyRes getListenKey() {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.LISTEN_KEY.getUrl())
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ListenKeyRes.class);
    }

    public void updateListenKey() {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.LISTEN_KEY.getUrl())
                        .build())
                .headers(httpHeaders -> httpHeaders
                        .add("X-MBX-APIKEY", apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
    }

    // TODO 하루마다 스케쥴링 돌리자 -> redis에 저장
    public String getLeverageBracket(LeverageBracketReq req) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BinanceURL.LEVERAGE_BRACKET.getUrl())
                        .query(req.toQueryString())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    private String makeSignature(String data) {
        Mac sha256Hmac;
        try {
            sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }


        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
