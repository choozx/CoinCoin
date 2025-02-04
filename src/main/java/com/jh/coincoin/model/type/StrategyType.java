package com.jh.coincoin.model.type;

import com.jh.coincoin.util.CodeEnum;
import com.jh.coincoin.util.CodeEnumFinder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

/**
 * Created by dale on 2024-11-22.
 */
public class StrategyType {

    public enum OrderStrategyType implements CodeEnum<Integer> {
        REVERSE_TREND_USING_RSI(1, "reverse_trend_using_rsi"),
        OVER_SOLD(2, "over_sold"),
        ;

        private final int code;
        private final String name;

        OrderStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return name;
        }

        @Converter
        public static class OrderStrategyConverter implements AttributeConverter<OrderStrategyType, Integer> {

            @Override
            public Integer convertToDatabaseColumn(OrderStrategyType orderStrategyType) {
                return orderStrategyType.code;
            }

            @Override
            public OrderStrategyType convertToEntityAttribute(Integer integer) {
                return CodeEnumFinder.findByCode(OrderStrategyType.class, integer);
            }
        }
    }

    public enum BuyStrategyType implements CodeEnum<Integer> {
        STOP_AND_LIMIT(1, "stop_and_limit"),
        ;

        private final int code;
        private final String name;

        BuyStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return name;
        }

        @Converter
        public static class BuyStrategyConverter implements AttributeConverter<BuyStrategyType, Integer> {

            @Override
            public Integer convertToDatabaseColumn(BuyStrategyType buyStrategyType) {
                return buyStrategyType.code;
            }

            @Override
            public BuyStrategyType convertToEntityAttribute(Integer integer) {
                return CodeEnumFinder.findByCode(BuyStrategyType.class, integer);
            }
        }
    }

    @Getter
    public enum RiskRewardRatioType implements CodeEnum<Integer>{
        FIXED_RATIO(1),
        PEAK_RATIO(2),
        ;

        private final int code;

        RiskRewardRatioType(int code) {
            this.code = code;
        }

        @Override
        public String getKey() {
            return "";
        }

        @Converter
        public static class RiskRewardRatioConverter implements AttributeConverter<RiskRewardRatioType, Integer> {

            @Override
            public Integer convertToDatabaseColumn(RiskRewardRatioType riskRewardRatioType) {
                return riskRewardRatioType.code;
            }

            @Override
            public RiskRewardRatioType convertToEntityAttribute(Integer integer) {
                return CodeEnumFinder.findByCode(RiskRewardRatioType.class, integer);
            }
        }
    }

}
