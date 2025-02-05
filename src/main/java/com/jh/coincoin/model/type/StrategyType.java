package com.jh.coincoin.model.type;

import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.CodeEnum;
import com.jh.coincoin.util.CodeEnumFinder;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dale on 2024-11-22.
 */
public class StrategyType {

    public enum OrderStrategyType implements CodeEnum<Integer> {
        REVERSE_TREND_USING_RSI(1, "reverse_trend_using_rsi", "역추세 매매법 (RSI)"),
        TEST(2, "test", "테스트 기법")
        ;

        private final int code;
        private final String key;
        @Getter
        private final String description;

        OrderStrategyType(int code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return key;
        }

        public static OrderStrategyType of(String key) {
            return Arrays.stream(values()).filter(type -> type.key.equals(key)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 전략"));
        }

        public static List<OptionObject> toOptionObjectList() {
            return Arrays.stream(values())
                    .map(type -> OptionObject.builder()
                            .text(PlainTextObject.builder()
                                    .text(type.description)
                                    .build())
                            .value(type.key)
                            .build())
                    .toList();
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
        STOP_AND_LIMIT(1, "stop_and_limit", "손익절 매수 전략"),
        ;

        private final int code;
        private final String key;
        @Getter
        private final String description;

        BuyStrategyType(int code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return key;
        }

        public static BuyStrategyType of(String key) {
            return Arrays.stream(values()).filter(type -> type.key.equals(key)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 전략"));
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
    public enum RiskRewardRatioType implements CodeEnum<Integer> {
        FIXED_RATIO(1, "fixed_ratio", "고정 손익절 비율"),
        PEAK_RATIO(2, "peak_ratio", "저점/고점 대비 손익절 비율"),
        ;

        private final int code;
        private final String key;
        @Getter
        private final String description;

        RiskRewardRatioType(int code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return key;
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
