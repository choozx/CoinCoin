package com.jh.coincoin.model.type;

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
