package com.jh.coincoin.service.strategy.buy;

import com.jh.coincoin.model.Binance.ModifyLeverageReq;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.Binance.PositionInfoReq;
import com.jh.coincoin.model.Binance.TickerPriceRes;
import com.jh.coincoin.model.Binance.TickerPriceReq;
import com.jh.coincoin.model.Binance.AccountBalanceReq;
import com.jh.coincoin.model.Binance.AccountBalanceRes;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.external.BinanceFutureAPIService;
import com.jh.coincoin.util.CommonUtil;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dale on 2024-11-22.
 * 손절과 익절을 한번에 걸어둠
 */

@Slf4j
@Service
public class StopAndLimitBuyStrategy extends BuyStrategy {
    private final BinanceFutureAPIService binanceFutureAPIService;

    public StopAndLimitBuyStrategy(AdminService adminService, BinanceFutureAPIService binanceFutureAPIService) {
        super(adminService);
        this.binanceFutureAPIService = binanceFutureAPIService;
    }

    @Override
    public BuyStrategyType getType() {
        return BuyStrategyType.STOP_AND_LIMIT;
    }

    @Override
    public void order(Symbol symbol, Side side) {
        // 1.주문된 상태 체크 redis에서 주문정보 get-> 주문된 상태면 return
        long now = DateTimeUtil.getCurrentTimeMillis();

        AccountBalanceReq accountBalanceReq = AccountBalanceReq.builder()
                .timestamp(now)
                .build();

        TickerPriceReq tickerPriceReq = TickerPriceReq.builder()
                .symbol(symbol)
                .build();

        AccountBalanceRes accountBalance = binanceFutureAPIService.getAccountBalance(accountBalanceReq);
        TickerPriceRes tickerPrice = binanceFutureAPIService.getTickerPrice(tickerPriceReq);

        float availableBalance = accountBalance.getAvailableBalance();
        float orderBalanceRatio = adminService.getOrderBalanceRatio();
        int leverage = adminService.getLeverage();

        // 레버리지 조정
        ModifyLeverageReq modifyLeverageReq = ModifyLeverageReq.builder()
                .symbol(symbol)
                .leverage(leverage)
                .timestamp(now)
                .build();
        binanceFutureAPIService.modifyLeverage(modifyLeverageReq);
        log.info("레버리지 조정 : x{}", leverage);

        // FIXME 추후 MIN_ORDER_AMOUNT는 fapi/v1/exchangeInfo의 min_national 필드값을 참조해서 써야됨
        float orderBalance = Math.max(leverage * (orderBalanceRatio * availableBalance), GlobalConst.MIN_ORDER_AMOUNT);

        double quantity = orderBalance / tickerPrice.getPrice();

        // TODO 여기서 quantity가 최소 주문 갯수를 넘지 못하면 slack 알림후 return

        // 최초 주문
        NewOrderReq order = NewOrderReq.builder()
                .symbol(symbol)
                .side(side)
                .type(Order.MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .timestamp(now)
                .build();
        binanceFutureAPIService.newOrder(order);

        // 주문 확인
        PositionInfoReq positionInfoReq = PositionInfoReq.builder()
                .symbol(symbol)
                .timestamp(now)
                .build();
        List<PositionInfoRes> positionInfoResList = binanceFutureAPIService.getPositionInfo(positionInfoReq);
        PositionInfoRes positionInfoRes = positionInfoResList.get(0);   // 이 전략의 경우에는 포지션을 하나만 잡을것이기 인덱스 0에서 가져온다
        log.info("주문 정보 확인 : {}", positionInfoRes);

        double entryPrice = positionInfoRes.getEntryPrice();

        // 익절가 주문
        NewOrderReq tkOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.TAKE_PROFIT_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(calcPrice(side, Order.TAKE_PROFIT_MARKET, entryPrice), 2))
                .timestamp(now)
                .build();
        binanceFutureAPIService.newOrder(tkOrder);

        // 손절가 주문
        NewOrderReq slOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.STOP_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(calcPrice(side, Order.STOP_MARKET, entryPrice), 2))
                .timestamp(now)
                .build();
        binanceFutureAPIService.newOrder(slOrder);
    }
}
