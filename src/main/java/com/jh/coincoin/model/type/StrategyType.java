package com.jh.coincoin.model.type;

/**
 * Created by dale on 2024-11-22.
 */
public class StrategyType {

    public enum OrderStrategyType {
        OVER_BOUGHT(1, "over_bought"),
        OVER_SOLD(2, "over_sold"),
        ;

        private int code;
        private String name;

        OrderStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    public enum BuyStrategyType {
        STOP_AND_LIMIT(1, "stop_and_limit"),
        ;

        private int code;
        private String name;

        BuyStrategyType(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

}
