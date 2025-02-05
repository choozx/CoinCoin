package com.jh.coincoin.model;

import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.entity.RiskRewardRatioStrategyEntity;
import com.jh.coincoin.entity.TradeStrategyEntity;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
    }

    @Getter
    @Builder
    public static class OrderParamDto {
        private Symbol symbol;
        private Side side;
        private Interval interval;
        private int leverage;
        private RiskRewardRatioDto riskRewardRatioDto;
        private double orderBalanceRatio;
    }

    @Data
    public static class BuyStrategyDto {
        private long idx;
        private BuyStrategyType type;
        private RiskRewardRatioDto riskRewardRatioDto;
        private int leverage;
        private double orderBalanceRatio;

        public static BuyStrategyDto create(BuyStrategyEntity buyStrategyEntity) {
            BuyStrategyDto buyStrategyDto = new BuyStrategyDto();
            buyStrategyDto.idx = buyStrategyEntity.getIdx();
            buyStrategyDto.type = buyStrategyEntity.getType();
            buyStrategyDto.leverage = buyStrategyEntity.getLeverage();
            buyStrategyDto.orderBalanceRatio = buyStrategyEntity.getOrderBalanceRatio();
            buyStrategyDto.riskRewardRatioDto = RiskRewardRatioDto.create(buyStrategyEntity.getRiskRewardRatioStrategyEntity());

            return buyStrategyDto;
        }
    }

    @Data
    public static class RiskRewardRatioDto {
        private RiskRewardRatioType type;
        private double stop;   // 손절비율
        private double limit;  // 익절비율

        public static RiskRewardRatioDto create(RiskRewardRatioStrategyEntity riskRewardRatioStrategyEntity) {
            RiskRewardRatioDto riskRewardRatioDto = new RiskRewardRatioDto();
            riskRewardRatioDto.type = riskRewardRatioStrategyEntity.getType();
            riskRewardRatioDto.stop = riskRewardRatioStrategyEntity.getStop();
            riskRewardRatioDto.limit = riskRewardRatioStrategyEntity.getLimit();
            return riskRewardRatioDto;
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

    @Data
    @Builder
    public static class RSIValue {
        private int overBought;
        private int overSell;
    }
}
