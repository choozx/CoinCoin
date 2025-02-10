package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Symbol.SymbolConverter;
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
 * Created by dale on 2025-02-10.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "indicator")
public class IndicatorEntity implements Persistable<Long> {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    @Column(name = "type")
    private int type;
    @Column(name = "symbol") @Convert(converter = SymbolConverter.class)
    private Symbol symbol;
    @Column(name = "`interval`")
    private int interval;
    @Column(name = "open_time")
    private long openTime;
    @Column(name = "`value`")
    private String value;

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
