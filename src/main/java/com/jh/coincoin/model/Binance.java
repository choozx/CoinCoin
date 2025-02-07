package com.jh.coincoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.entity.TradeLogEntity;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.NewOrderResp;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.PositionSide;
import com.jh.coincoin.model.type.BinanceType.PriceMatch;
import com.jh.coincoin.model.type.BinanceType.SelfTradePreventionMode;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.TimeInForce;
import com.jh.coincoin.model.type.BinanceType.TriggerSource;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by dale on 2024-11-26.
 */
public class Binance {

    public static class BaseReq {
        public String toQueryString() {
            StringJoiner queryString = new StringJoiner("&");
            Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // Allow access to private fields
                try {
                    Object value = field.get(this);
                    if (value != null) {
                        String stringValue = value instanceof Enum || value instanceof Boolean || value instanceof Number
                                ? value.toString()
                                : URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);

                        queryString.add(field.getName() + "=" + stringValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                }
            }

            return queryString.toString();
        }
    }

    @Builder
    public static class PositionInfoReq extends BaseReq {
        private Symbol symbol;
        private Long recvWindow;
        private Long timestamp;
    }

    @Data
    public static class PositionInfoRes {
        private Symbol symbol;
        private PositionSide positionSide;
        private double positionAmount;
        private double entryPrice;
        private double breakEvenPrice;
        private double markPrice;
        private double unRealizedProfit;
        private double liquidationPrice;
        private double isolatedMargin;
        private double notional;
        private long updateTime;

        public PositionInfoRes(Map<String, Object> rawData) {
            this.symbol = Symbol.of((String) rawData.get("symbol"));
            this.positionSide = PositionSide.of((String) rawData.get("positionSide"));
            this.positionAmount = Float.parseFloat((String) rawData.get("positionAmt"));
            this.entryPrice = Float.parseFloat((String) rawData.get("entryPrice"));
            this.breakEvenPrice = Float.parseFloat((String) rawData.get("breakEvenPrice"));
            this.markPrice = Float.parseFloat((String) rawData.get("markPrice"));
            this.unRealizedProfit = Float.parseFloat((String) rawData.get("unRealizedProfit"));
            this.liquidationPrice = Float.parseFloat((String) rawData.get("liquidationPrice"));
            this.isolatedMargin = Float.parseFloat((String) rawData.get("isolatedMargin"));
            this.notional = Float.parseFloat((String) rawData.get("notional"));
            this.updateTime = (long) rawData.get("updateTime");
        }

        public String toDescription() {
            return String.format("[%s] %s 수량:%-10.3f | 진입가격:%-10.3f | 청산가격:%-10.3f", symbol, positionSide.getName(), positionAmount, entryPrice, liquidationPrice);
        }
    }

    @Getter
    @Setter
    @Builder
    public static class NewOrderReq extends BaseReq {
        @NotNull
        private Symbol symbol;
        @NotNull
        private Side side;
        private PositionSide positionSide;
        @NotNull
        private Order type;
        private TimeInForce timeInForce;
        private Double quantity;    // 이 숫자는 소수점 3자리까지만 허용함
        private Boolean reduceOnly;
        private Double price;
        private String newClientOrderId;
        private Double stopPrice;   // 이 숫자는 소수점 2자리까지만 허용함
        private Boolean closePosition;
        private Double activationPrice;
        private Double callbackRate;
        private TriggerSource workingType;
        private Boolean priceProtect;
        private NewOrderResp newOrderRespType;
        private PriceMatch priceMatch;
        private SelfTradePreventionMode selfTradePreventionMode;
        private Long goodTillDate;
        private Long recvWindow;
        private Long timestamp;
    }

    @Data
    public static class NewOrderRes {
        private String clientOrderId;
        private double cumQty;
        private double cumQuote;
        private double executedQty;
        private Long orderId;
        private double avgPrice;
        private double origQty;
        private double price;
        private boolean reduceOnly;
        private Side side;
        private PositionSide positionSide;
        private String status;
        private double stopPrice;
        private boolean closePosition;
        private Symbol symbol;
        private TimeInForce timeInForce;
        private Order type;
        private Order origType;
        private double activatePrice;
        private double priceRate;
        private long updateTime;
        private TriggerSource workingType;
        private boolean priceProtect;
        private PriceMatch priceMatch;
        private SelfTradePreventionMode selfTradePreventionMode;
        private long goodTillDate;

        public NewOrderRes(Map<String, Object> rawData) {
            this.clientOrderId = (String) rawData.get("clientOrderId");
//            this.cumQty = Double.parseDouble((String) rawData.get("cumQty"));
            this.cumQuote = Double.parseDouble((String) rawData.get("cumQuote"));
            this.executedQty = Double.parseDouble((String) rawData.get("executedQty"));
            this.orderId = (Long) rawData.get("orderId");
            this.avgPrice = Double.parseDouble((String) rawData.get("avgPrice"));
//            this.origQty = rawData.get("");
//            this.price = rawData.get("");
//            this.reduceOnly = rawData.get("");
            this.side = Side.valueOf((String) rawData.get("side"));
            this.positionSide = PositionSide.of((String) rawData.get("positionSide"));
//            this.status = rawData.get("");
//            this.stopPrice = rawData.get("");
//            this.closePosition = rawData.get("");
            this.symbol = Symbol.of((String) rawData.get("symbol"));
//            this.timeInForce = rawData.get("");
//            this.type = rawData.get("");
//            this.origType = rawData.get("");
//            this.activatePrice = rawData.get("");
//            this.priceRate = rawData.get("");
//            this.updateTime = rawData.get("");
//            this.workingType = rawData.get("");
//            this.priceProtect = rawData.get("");
//            this.priceMatch = rawData.get("");
//            this.selfTradePreventionMode = rawData.get("");
//            this.goodTillDate = rawData.get("");
        }
    }

    @Builder
    public static class CheckOrderReq extends BaseReq {
        private Symbol symbol;
        private Long orderId;
        private String origClientOrderId;
        private Long recvWindow;
        private Long timestamp;
    }

    @Builder
    public static class AccountBalanceReq extends BaseReq {
        private Long recvWindow;
        private Long timestamp;
    }

    @Getter
    public static class AccountBalanceRes {
        private String accountAlias;
        private String asset;
        private float balance;
        private float crossWalletBalance;
        private float crossUnPnl;
        private float availableBalance;
        private float maxWithdrawAmount;
        private boolean marginAvailable;
        private long updateTime;

        public AccountBalanceRes(Map<String, Object> rawData) {
            this.accountAlias = (String) rawData.get("accountAlias");
            this.asset = (String) rawData.get("asset");
            this.balance = Float.parseFloat((String) rawData.get("balance"));
            this.crossWalletBalance = Float.parseFloat((String) rawData.get("crossWalletBalance"));
            this.crossUnPnl = Float.parseFloat((String) rawData.get("crossUnPnl"));
            this.availableBalance = Float.parseFloat((String) rawData.get("availableBalance"));
            this.maxWithdrawAmount = Float.parseFloat((String) rawData.get("maxWithdrawAmount"));
            this.marginAvailable = (boolean) rawData.get("marginAvailable");
            this.updateTime = (long) rawData.get("updateTime");
        }
    }

    @Builder
    public static class TickerPriceReq extends BaseReq {
        private Symbol symbol;
    }

    @Getter
    public static class TickerPriceRes {
        private Symbol symbol;
        private float price;
        private long time;
    }

    @Builder
    public static class ModifyLeverageReq extends BaseReq {
        private Symbol symbol;
        private int leverage;
        private long timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        @JsonProperty("e")
        private String eventType; // 이벤트 타입 (ORDER_TRADE_UPDATE)
        @JsonProperty("E")
        private long eventTime;   // 이벤트 발생 시간
        @JsonProperty("T")
        private long requestTime;   // 클라이언트 요청 시간
        @JsonProperty("o")
        private JsonNode objects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetails {
        @JsonProperty("s")
        private Symbol symbol; // 거래쌍 (예: BTCUSDT)
        @JsonProperty("c")
        private String clientOrderId; // 주문 아이디
        @JsonProperty("S")
        private Side side; // 주문 종류 (SELL, BUY)
        @JsonProperty("o")
        private Order order; // 주문 유형 (TAKE_PROFIT_MARKET, STOP_MARKET 등)
        @JsonProperty("f")
        private TimeInForce timeInForce; // 주문의 유효 기간 (Good Till Cancelled)
        @JsonProperty("q")
        private double origQty; // 주문 수량
        @JsonProperty("p")
        private double price; // 주문 가격
        @JsonProperty("ap")
        private double avgPrice; // 실제 체결 가격
        @JsonProperty("sp")
        private double stopPrice; // Stop Price
        @JsonProperty("X")
        private OrderState orderState; // 주문 상태 (FILLED, CANCELED, PENDING 등)
        @JsonProperty("t")
        private long orderTime; // 주문 발생 시간
        @JsonProperty("T")
        private long timestamp; // 클라이언트 타임스탬프
        @JsonProperty("i")
        private long orderId; // 주문 아이디
        @JsonProperty("l")
        private double executedQty; // 체결된 수량
        @JsonProperty("z")
        private String cumQty; // 체결된 수량 총합
    }

    @Getter
    public static class ListenKeyRes {
        private String listenKey;
    }

    @Builder
    public static class CancelOpenOrderReq extends BaseReq {
        private Symbol symbol;
        private Long recvWindow;
        private Long timestamp;
    }

    @Data
    public static class TradeLogDto {
        private long idx;
        private Symbol symbol;
        private Side side;
        private BuyStrategyType buyStrategyType;
        private OrderState orderState;
        private double avgPrice;
        private double positionQuantity;
        private Double closePrice;
        private Double pnl;
        private String option;  // 매수 전략에 사용될 값 ex) 물타기 전략-> 물탄 횟수 저장

        public static TradeLogDto to(TradeLogEntity entity) {
            TradeLogDto dto = new TradeLogDto();
            dto.idx = entity.getIdx();
            dto.symbol = entity.getSymbol();
            dto.buyStrategyType = entity.getBuyStrategyType();
            dto.orderState = entity.getOrderState();
            dto.avgPrice = entity.getAvgPrice();
            dto.positionQuantity = entity.getPositionQuantity();
            dto.closePrice = entity.getClosePrice();
            dto.pnl = entity.getPnl();
            dto.option = entity.getOption();
            return dto;
        }
    }
}
