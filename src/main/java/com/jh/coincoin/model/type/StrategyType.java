package com.jh.coincoin.model.type;

import com.jh.coincoin.support.ServerException;

import java.util.Arrays;

/**
 * Created by dale on 2024-11-22.
 */
public class StrategyType {

    public enum OrderStrategyType {
        REVERSE_TREND_USING_RSI(1, "reverse_trend_using_rsi"),
        OVER_SOLD(2, "over_sold"),
        ;

        private final int code;
        private final String name;

        OrderStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static OrderStrategyType of(String name) {
            return Arrays.stream(values()).filter(type -> type.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 전략"));
        }
    }

    public enum BuyStrategyType {
        STOP_AND_LIMIT(1, "stop_and_limit"),
        ;

        private final int code;
        private final String name;

        BuyStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

}
