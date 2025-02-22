package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Symbol.SymbolConverter;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.model.type.IndicatorType.IndicatorConverter;
import jakarta.annotation.PostConstruct;
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
@Table(name = "indicator")
public class IndicatorEntity implements Persistable<Long> {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    @Column(name = "type") @Convert(converter = IndicatorConverter.class)
    private IndicatorType type;
    @Column(name = "symbol") @Convert(converter = SymbolConverter.class)
    private Symbol symbol;
    @Column(name = "`interval`")
    private int interval;
    @Column(name = "open_time")
    private long openTime;
    @Column(name = "`value`")
    private String value;           // DB 저장용 지표값

    @Transient
    private String[] valueArray;    // 앱에서 사용할 지표값

    @PostLoad
    public void init() {
        valueArray = value.split("\\|");
    }

    public static IndicatorEntity create(IndicatorType type, Symbol symbol, int interval, long openTime, String value) {
        IndicatorEntity entity = new IndicatorEntity();
        entity.type = type;
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
