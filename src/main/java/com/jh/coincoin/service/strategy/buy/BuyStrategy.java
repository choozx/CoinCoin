package com.jh.coincoin.service.strategy.buy;

import com.jh.coincoin.model.Strategy.RiskRewardRatioDto;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;

/**
 * Created by dale on 2024-11-22.
 * 이 클레스는 예산을 어떻게 분배할지에 대한 클레스이다
 */

public interface BuyStrategy {

    BuyStrategyType getType();
//    void order(Symbol symbol, Side side);
    void order(Symbol symbol, Side side, int leverage, RiskRewardRatioDto riskRewardRatioDto, double orderBalanceRatio);

//    protected Double calcPrice(Side side, Order order, double entryPrice) {
//        Pair<Double, Double> riskRewardRatio = adminService.getRiskRewardRatio();
//        double ratio = order.equals(Order.TAKE_PROFIT_MARKET) ? riskRewardRatio.getRight() : riskRewardRatio.getLeft();
//        double priceChange = entryPrice * ratio / 100;
//        boolean isProfitOrder = order.equals(Order.TAKE_PROFIT_MARKET);
//
//        return side.equals(Side.BUY)
//                ? entryPrice + (isProfitOrder ? priceChange : -priceChange)
//                : entryPrice - (isProfitOrder ? priceChange : -priceChange);
//    }
}
