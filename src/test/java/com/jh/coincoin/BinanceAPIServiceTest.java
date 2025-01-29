package com.jh.coincoin;

import com.jh.coincoin.model.Binance;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.external.BinanceFutureAPIService;
import com.jh.coincoin.service.strategy.buy.StopAndLimitBuyStrategy;
import com.jh.coincoin.util.CommonUtil;
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
    private final BinanceFutureAPIService apiService;
    private final AdminService adminService;
    private final StopAndLimitBuyStrategy buyStrategy;

    @Test
    public void ping(){
        Long serverTime = restClient.get()
                .uri(BinanceURL.BASE_URL + "/v1/ping")
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
    public void 주문_확인후_익절가_주문() {
        Symbol symbol = Symbol.ETHUSDT;
        Side side = Side.BUY;
        long now = DateTimeUtil.getCurrentTimeMillis();

        Binance.PositionInfoReq req = Binance.PositionInfoReq.builder()
                .symbol(symbol)
                .timestamp(now)
                .build();
        var res = apiService.getPositionInfo(req);

        for (PositionInfoRes positionInfoRes: res) {
            log.info("{}", positionInfoRes);
        }
        PositionInfoRes positionInfoRes = res.get(0);
//
        var riskRewardRatio = adminService.getRiskRewardRatio();

        double avgPrice = positionInfoRes.getEntryPrice();
        double quantity = positionInfoRes.getPositionAmount();

        double rewardRatio = riskRewardRatio.getRight();
        NewOrderReq tkOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.TAKE_PROFIT_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(avgPrice + (avgPrice * rewardRatio / 100), 2))
                .timestamp(now)
                .build();
        apiService.newOrder(tkOrder);

        double riskRatio = riskRewardRatio.getLeft();
        NewOrderReq slOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.STOP_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(avgPrice - (avgPrice * riskRatio / 100), 2))
                .timestamp(now)
                .build();
        apiService.newOrder(slOrder);
    }

    @Test
    public void 진입_테스트() {
        Symbol symbol = Symbol.ETHUSDT;
        Side side = Side.BUY;

        buyStrategy.order(symbol, side);
    }
}
