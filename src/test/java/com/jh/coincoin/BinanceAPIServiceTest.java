package com.jh.coincoin;

import com.jh.coincoin.model.Binance;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import com.jh.coincoin.service.TradeService;
import com.jh.coincoin.service.external.BinanceAPIService;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestClient;

/**
 * Created by dale on 2024-11-26.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class BinanceAPIServiceTest {

    private final RestClient restClient = RestClient.create();
    private final BinanceAPIService apiService;
    private final TradeService tradeService;

    @Test
    public void ping(){
        Long serverTime = restClient.get()
                .uri(BinanceURL.HTTPS_BASE_URL + "/v1/ping")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Long.class);

        System.out.println(serverTime);
    }

    @Test
    public void 포지션_정보_가져오기() {
        long now = DateTimeUtil.getCurrentTimeMillis();
        Binance.PositionInfoReq req = Binance.PositionInfoReq.builder()
                .symbol(Symbol.ETHUSDT)
                .timestamp(now)
                .build();
        var res = apiService.getPositionInfo(req);

        for (PositionInfoRes positionInfoRes: res) {
            log.info("{}", positionInfoRes);
        }
    }

    @Test
    public void 테스트_주문() {
        NewOrderReq req = NewOrderReq.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .type(Order.MARKET)
                .quantity(0.006)
                .timestamp(DateTimeUtil.getCurrentTimeMillis())
                .build();

        apiService.newTestOrder(req);
    }

    @Test
    public void 주문() {
        NewOrderReq req = NewOrderReq.builder()
                .symbol(Symbol.ETHUSDT)
                .side(Side.BUY)
                .type(Order.MARKET)
                .quantity(0.007)
                .timestamp(DateTimeUtil.getCurrentTimeMillis())
                .build();

        var res = apiService.newOrder(req);

        log.info("주문 정보:{}", res);
    }

    @Test
    public void 내_통잔_잔고_확인() {
        Binance.AccountBalanceReq req = Binance.AccountBalanceReq.builder()
                .timestamp(DateTimeUtil.getCurrentTimeMillis())
                .build();

        var res = apiService.getAccountBalance(req);

        log.info("통장 잔고 : {}", res.toString());
    }

    @Test
    public void 실시간_가격() {
        Binance.TickerPriceReq tickerPriceReq = Binance.TickerPriceReq.builder()
                .symbol(Symbol.BTCUSDT)
                .build();

        var res = apiService.getTickerPrice(tickerPriceReq);

        log.info("실시간 가격 : {}", res);
    }

    @Test
    public void 모든_주문_가져오기() {
        Binance.CheckOrderReq checkOrderReq = Binance.CheckOrderReq.builder()
                .symbol(Symbol.ETHUSDT)
                .timestamp(DateTimeUtil.getCurrentTimeMillis())
                .build();
        var allOrderList = apiService.getAllOrder(checkOrderReq);

        for (var orderInfo: allOrderList) {
            log.info("order info: {}", orderInfo);
        }
    }

    @Test
    public void 자동매매_테스트() {
        // 돌리기 전에 테이블 만들고 데이터 넣어야함
        tradeService.tradeV2();
    }
}
