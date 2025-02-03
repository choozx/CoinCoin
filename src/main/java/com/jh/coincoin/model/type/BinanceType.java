package com.jh.coincoin.model.type;

import com.fasterxml.jackson.annotation.JsonValue;
import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.CodeEnum;
import com.jh.coincoin.util.CodeEnumFinder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

/**
 * Created by dale on 2024-09-11.
 */
public class BinanceType {

    public enum Symbol implements CodeEnum<Integer> {
        BTCUSDT(1, "BTCUSDT"),
        ETHUSDT(2, "ETHUSDT"),
        SOLUSDT(3, "SOLUSDT"),
        XRPUSDT(4, "XRPUSDT"),
        DOGEUSDT(5, "DOGEUSDT"),
        TRXUSDT(6, "TRXUSDT"),
        BIGTIMEUSDT(7, "BIGTIMEUSDT"),
        NOTUSDT(8, "NOTUSDT"),
        CHZUSDT(9, "CHZUSDT"),
        FAIL(999, ""),
        ;

        private int code;
        private String name;

        Symbol(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Symbol of(String name) {
            return Arrays.stream(values()).filter(symbol -> symbol.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "등록되지 않은 코인"));
        }

        public static Symbol of(int code) {
            return Arrays.stream(values()).filter(symbol -> symbol.code == code).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "등록되지 않은 코인"));
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return name;
        }

        @JsonValue
        public String getName() {
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

        public String getName() {
            return name;
        }

        public int getMinute() {
            return minute;
        }

        public static Interval of(String name) {
            return Arrays.stream(values()).filter(interval -> interval.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "지원하지 않는 캔들봉"));
        }

        public static Interval of(int minute) {
            return Arrays.stream(values()).filter(interval -> interval.minute == minute).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "지원하지 않는 캔들봉"));
        }
    }

    public enum PositionSide {
        BOTH(0, "BOTH"),
        LONG(1, "LONG"),
        SHORT(2, "SHORT"),
        ;

        private int code;
        private String name;

        PositionSide(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static PositionSide of(String name) {
            return Arrays.stream(values()).filter(positionSide -> positionSide.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "지원하지 않는 positionSide"));
        }
    }

    public enum Side {
        BUY,
        SELL,
        ;

        public static Side reverse(Side side) {
            if (side.equals(BUY))
                return SELL;
            else
                return BUY;
        }
    }

    // 여러 주문 타입이 있지만, 손절/익절 주문은 왠만하면 STOP_MARKET, TAKE_PROFIT_MARKET을 사용한다.
    public enum Order {
        LIMIT,
        MARKET,
        STOP,
        TAKE_PROFIT,
        STOP_MARKET,
        TAKE_PROFIT_MARKET,
        TRAILING_STOP_MARKET,
        ;
    }

    public enum TimeInForce {
        GTC,
        IOC,
        FOK,
        ;
    }

    public enum TriggerSource {
        CONTRACT_PRICE,
        MARK_PRICE,
        ;
    }

    public enum NewOrderResp {
        ACK,
        RESULT,
        ;
    }

    public enum PriceMatch {
        NONE,
        OPPONENT,
        OPPONENT_5,
        OPPONENT_10,
        OPPONENT_20,
        QUEUE,
        QUEUE_5,
        QUEUE_10,
        QUEUE_20,
        ;
    }

    public enum SelfTradePreventionMode {
        NONE,
        EXPIRE_TAKER,
        EXPIRE_MAKER,
        EXPIRE_BOTH,
        ;
    }

    public enum BinanceURL {
        BASE_URL("https://fapi.binance.com/fapi"),
        GET_POSITION_INFO("/v3/positionRisk"),
        GET_ACCOUNT_BALANCE("/v3/balance"),
        GET_TICKER_PRICE("/v2/ticker/price"),
        NEW_TEST_ORDER("/v1/order/test"),
        NEW_ORDER("/v1/order"),
        GET_OPEN_ORDER("/v1/openOrder"),
        GET_ALL_ORDER("/v1/allOrders"),
        MODIFY_LEVERAGE("/v1/leverage"),
        ;

        private final String url;

        BinanceURL(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
