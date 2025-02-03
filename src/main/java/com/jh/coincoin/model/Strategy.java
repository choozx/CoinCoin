package com.jh.coincoin.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.entity.StrategyEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import lombok.Data;

public class Strategy {

    @Data
    public static class StrategyDto {
        private long idx;
        private Symbol symbol;
        private Interval interval;
        private OrderStrategyDto orderStrategy;
        private BuyStrategyDto buyStrategy;

        public static StrategyDto create(StrategyEntity strategyEntity) {
            StrategyDto strategyDto = new StrategyDto();
            strategyDto.idx = strategyEntity.getIdx();
            strategyDto.symbol = strategyEntity.getSymbol();
            strategyDto.interval = Interval.of(strategyEntity.getInterval());
            strategyDto.orderStrategy = OrderStrategyDto.create(strategyEntity.getOrderStrategyEntity());
            strategyDto.buyStrategy = BuyStrategyDto.create(strategyEntity.getBuyStrategyEntity());
            return strategyDto;
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

    @Data
    public static class BuyStrategyDto {
        private long idx;
        private BuyStrategyType type;
        private RiskRewardRatioDto riskRewardRatioDto;
        private int leverage;
        private double orderBalanceRatio;

        public static BuyStrategyDto create(BuyStrategyEntity buyStrategyEntity) {
            ObjectMapper objectMapper = new ObjectMapper();

            BuyStrategyDto buyStrategyDto = new BuyStrategyDto();
            buyStrategyDto.idx = buyStrategyEntity.getIdx();
            buyStrategyDto.type = buyStrategyEntity.getType();
            buyStrategyDto.leverage = buyStrategyEntity.getLeverage();
            buyStrategyDto.orderBalanceRatio = buyStrategyEntity.getOrderBalanceRatio();

            try {
                buyStrategyDto.riskRewardRatioDto = objectMapper.readValue(buyStrategyEntity.getRiskRewardRatio(), RiskRewardRatioDto.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return buyStrategyDto;
        }
    }


    @Data
    public static class RiskRewardRatioDto {
        private RiskRewardRatioType type;
        private int stop;   // 손절비율
        private int limit;  // 익절비율
    }

    @Data
    public static class RSI {
        private int overbought;
        private int oversold;
    }
}
