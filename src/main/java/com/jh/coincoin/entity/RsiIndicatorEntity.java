package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Symbol.SymbolConverter;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.model.type.IndicatorType.IndicatorConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * Created by dale on 2025-02-10.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "rsi_indicator")
public class RsiIndicatorEntity implements Persistable<Long> {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    @Column(name = "symbol") @Convert(converter = SymbolConverter.class)
    private Symbol symbol;
    @Column(name = "`interval`")
    private int interval;
    @Column(name = "open_time")
    private long openTime;
    @Column(name = "`value`")
    private double value;           // DB 저장용 지표값

    public static RsiIndicatorEntity create(Symbol symbol, int interval, long openTime, double value) {
        RsiIndicatorEntity entity = new RsiIndicatorEntity();
        entity.symbol = symbol;
        entity.interval = interval;
        entity.openTime = openTime;
        entity.value = value;
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
