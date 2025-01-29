package com.jh.coincoin.service.strategy.order;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.indicator.RSIIndicator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import static com.jh.coincoin.model.type.StrategyType.OrderStrategyType.OVER_BOUGHT;

/**
 * Created by dale on 2024-11-22.
 * rsi를 이용한 주문전략
 */

@Service
@RequiredArgsConstructor
public class OverBoughtOrderStrategy implements OrderStrategy {

    private final RSIIndicator rsiIndicator;
    private final AdminService adminService;

    @Override
    public OrderStrategyType getType() {
        return OVER_BOUGHT;
    }

    @Override
    public Pair<Boolean, Side> isHit(Symbol symbol) {
        BinanceType.Interval interval = adminService.getInterval();
        Double rsiRatio = rsiIndicator.getLastFigure(symbol, interval);

        Pair<Double, Double> orderRsiValuePair = adminService.getOrderRsiValuePair();

        if (rsiRatio >= orderRsiValuePair.getRight())
            return Pair.of(true, Side.SELL);

        if (rsiRatio <= orderRsiValuePair.getLeft())
            return Pair.of(true, Side.BUY);

        return Pair.of(false, null);
    }
}
