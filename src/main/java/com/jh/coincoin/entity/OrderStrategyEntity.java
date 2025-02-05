package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType.OrderStrategyConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "order_strategy")
public class OrderStrategyEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private long idx;
    @Column(name = "type")
    @Convert(converter = OrderStrategyConverter.class)
    private OrderStrategyType type;
    @Column(name = "target_value")
    private String targetValue;    // 주문 진입시 기준이 될 값.
    @Column(name = "interval")
    private int interval;

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
