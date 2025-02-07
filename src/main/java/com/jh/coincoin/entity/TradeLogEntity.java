package com.jh.coincoin.entity;

import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Side.SideConverter;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.OrderState.OrderStateConverter;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Symbol.SymbolConverter;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType.BuyStrategyConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * Created by dale on 2025-02-07.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "trade_log")
public class TradeLogEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private long idx;
    @Column(name = "symbol")
    @Convert(converter = SymbolConverter.class)
    private Symbol symbol;
    @Column(name = "side")
    @Convert(converter = SideConverter.class)
    private Side side;
    @Column(name = "buy_strategy_type")
    @Convert(converter = BuyStrategyConverter.class)
    private BuyStrategyType buyStrategyType;
    @Column(name = "order_state")
    @Convert(converter = OrderStateConverter.class)
    private OrderState orderState;
    @Column(name = "avg_price")
    private double avgPrice;
    @Column(name = "position_quantity")
    private double positionQuantity;
    @Column(name = "close_price")
    private Double closePrice;
    @Column(name = "pnl")
    private Double pnl;
    @Column(name = "`option`")
    private String option;  // 매수 전략에 사용될 값 ex) 물타기 전략-> 물탄 횟수 저장

    public static TradeLogEntity create(PositionInfoRes positionInfoRes, Side side, BuyStrategyType buyStrategyType) {
        TradeLogEntity entity = new TradeLogEntity();
        entity.symbol = positionInfoRes.getSymbol();
        entity.side = side;
        entity.buyStrategyType = buyStrategyType;
        entity.orderState = OrderState.NEW;
        entity.avgPrice = positionInfoRes.getEntryPrice();
        entity.positionQuantity = Math.abs(positionInfoRes.getPositionAmount());
        return entity;
    }

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
