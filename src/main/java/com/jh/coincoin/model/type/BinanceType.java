package com.jh.coincoin.model.type;

import com.jh.coincoin.util.CodeEnum;
import com.jh.coincoin.util.CodeEnumFinder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Created by dale on 2024-09-11.
 */
public class BinanceType {

    public enum Symbol implements CodeEnum<Integer> {
        BTC_USDT(1,"BTCUSDT"),
        ETH_USDT(2, "ETHUSDT"),
        ;

        private int code;
        private String name;
        Symbol(int code, String name) {
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
        public static class SymbolConverter implements AttributeConverter<Symbol, Integer> {

            @Override
            public Integer convertToDatabaseColumn(Symbol symbol) {
                return symbol.getCode();
            }

            @Override
            public Symbol convertToEntityAttribute(Integer code) {
                return CodeEnumFinder.findByCode(Symbol.class, code);
            }
        }
    }

    public enum Interval {
        ONE_MINUTE("1m", 1),
        FIVE_MINUTE("5m", 5),
        FIFTEEN_MINUTE("15m", 15),
        HALF_HOUR("30m", 30),
        HOUR("1h", 60),
        ;

        private String name;
        private int minute;
        Interval(String name, int minute) {
            this.name = name;
            this.minute = minute;
        }

        public String getName(){
            return name;
        }

        public int getMinute() {
            return minute;
        }
    }

    public enum BinanceURL {
        BASE_URL("https://fapi.binance.com/fapi"),
        GET_CANDLE("/v1/klines"),
        ;

        private final String url;
        BinanceURL(String url) {
            this.url = url;
        }

        public String getUrl(){
            return url;
        }
    }
}
