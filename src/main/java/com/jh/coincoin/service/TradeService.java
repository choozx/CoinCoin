package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.service.external.BinanceFutureAPIService;
import com.jh.coincoin.service.strategy.buy.BuyStrategy;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-11-22.
 */

@Service
@RequiredArgsConstructor
public class TradeService {

    // 매수를 위한 전략 서버스
    private final AdminService adminService;
    private final BinanceFutureAPIService binanceFutureAPIService;

    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;
    private Map<BuyStrategyType, BuyStrategy> buyStrategyMap;

    @Autowired
    public void setOrderStrategyMap(Set<OrderStrategy> orderStrategySet) {
        this.orderStrategyMap = orderStrategySet.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
    }

    @Autowired
    public void setBuyStrategyMap(Set<BuyStrategy> buyStrategySet) {
        this.buyStrategyMap = buyStrategySet.stream().collect(Collectors.toMap(BuyStrategy::getType, Function.identity()));
    }

    public void trade() {
        List<Symbol> trackingSymbolList = adminService.getTrackingSymbolList(); // FIXME 추후 지표를 위한 심볼리스트와 매수 진행을 위한 심볼리스트를 나눌 수 있음
        List<OrderStrategyType> followStrategyList = adminService.getFollowOrderStrategyList();
        BuyStrategyType buyStrategyType = adminService.getFollowBuyStrategy();

        for (Symbol symbol : trackingSymbolList) {

            for (OrderStrategyType orderStrategyType : followStrategyList) {
                OrderStrategy orderStrategy = orderStrategyMap.get(orderStrategyType);

                Pair<Boolean, Side> hit = orderStrategy.isHit(symbol);
                if (hit.getLeft()) {
                    // 주문 전략에 따른 주문
                    BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyType);
                    buyStrategy.order(symbol, hit.getRight());
                    break;
                }
            }
        }

        // need
        // 1. 매수를 위한 파라미터를 담을 클래스 -> adminService에서 관리
    }
}
