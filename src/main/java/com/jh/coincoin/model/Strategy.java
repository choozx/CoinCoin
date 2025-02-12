package com.jh.coincoin.model;

import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.entity.TradeStrategyEntity;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class Strategy {

    @Data
    public static class TradeStrategyDto {
        private long idx;
        private Symbol symbol;
        private Interval interval;
        private OrderStrategyDto orderStrategy;
        private BuyStrategyDto buyStrategy;

        public static TradeStrategyDto create(TradeStrategyEntity tradeStrategyEntity) {
            TradeStrategyDto tradeStrategyDto = new TradeStrategyDto();
            tradeStrategyDto.idx = tradeStrategyEntity.getIdx();
            tradeStrategyDto.symbol = tradeStrategyEntity.getSymbol();
            tradeStrategyDto.interval = Interval.of(tradeStrategyEntity.getInterval());
            tradeStrategyDto.orderStrategy = OrderStrategyDto.create(tradeStrategyEntity.getOrderStrategyEntity());
            tradeStrategyDto.buyStrategy = BuyStrategyDto.create(tradeStrategyEntity.getBuyStrategyEntity());
            return tradeStrategyDto;
        }
    }

    @Data
    public static class OrderStrategyDto {
        private long idx;
        private OrderStrategyType type;
        private String targetValue;

        public static OrderStrategyDto create(OrderStrategyEntity orderStrategyEntity) {
            OrderStrategyDto orderStrategyDto = new OrderStrategyDto();
            orderStrategyDto.idx = orderStrategyEntity.getIdx();
            orderStrategyDto.type = orderStrategyEntity.getType();
            orderStrategyDto.targetValue = orderStrategyEntity.getTargetValue();
            return orderStrategyDto;
        }

        public String toDescription() {
            return targetValue;
        }
    }

    @Getter
    @Builder
    public static class BuyParamDto {
        private Symbol symbol;
        private Side side;
        private Interval interval;
        private int leverage;
        private double orderBalanceRatio;
        private String targetValue;
    }

    @Data
    public static class BuyStrategyDto {
        private long idx;
        private BuyStrategyType type;
        private String targetValue;
        private int leverage;
        private double orderBalanceRatio;

        public static BuyStrategyDto create(BuyStrategyEntity buyStrategyEntity) {
            BuyStrategyDto buyStrategyDto = new BuyStrategyDto();
            buyStrategyDto.idx = buyStrategyEntity.getIdx();
            buyStrategyDto.type = buyStrategyEntity.getType();
            buyStrategyDto.targetValue = buyStrategyEntity.getTargetValue();
            buyStrategyDto.leverage = buyStrategyEntity.getLeverage();
            buyStrategyDto.orderBalanceRatio = buyStrategyEntity.getOrderBalanceRatio();

            return buyStrategyDto;
        }

        public String toDescription() {
            return targetValue + " | x" + leverage + " | " + orderBalanceRatio;
        }
    }

    @Getter
    @Builder
    public static class PriceCalculatorDto {
        private Symbol symbol;
        private Interval interval;
        private Side side;
        private Order order;
        private double entryPrice;
        private double riskRewardRatio;
    }

    @Getter
    @Builder
    public static class BackTestBuyDto {
        private long entryTime;
        private double avgPrice;
        private double stopPrice;
        private double limitPrice;
        private double liquidationPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RSIValue {
        private int overBought;
        private int overSell;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskRewardStrategy {
        private RiskRewardRatioType type;
        private double stop;
        private double limit;
    }
}
