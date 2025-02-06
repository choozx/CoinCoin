package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Symbol.SymbolConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "trade_strategy")
public class TradeStrategyEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private long idx;
    @Column(name = "symbol")
    @Convert(converter = SymbolConverter.class)
    private Symbol symbol;
    @Column(name = "`interval`")
    private int interval;
    @ManyToOne
    @JoinColumn(name = "order_strategy_idx")
    private OrderStrategyEntity orderStrategyEntity;
    @ManyToOne
    @JoinColumn(name = "buy_strategy_idx")
    private BuyStrategyEntity buyStrategyEntity;

    public static TradeStrategyEntity create(Symbol selectedSymbol, BinanceType.Interval selectedInterval, OrderStrategyEntity orderStrategy, BuyStrategyEntity buyStrategy) {
        TradeStrategyEntity entity = new TradeStrategyEntity();
        entity.symbol = selectedSymbol;
        entity.interval = selectedInterval.getMinute();
        entity.orderStrategyEntity = orderStrategy;
        entity.buyStrategyEntity = buyStrategy;
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
