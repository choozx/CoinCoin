package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType.BuyStrategyConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "buy_strategy")
public class BuyStrategyEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private long idx;
    @Column(name = "type")
    @Convert(converter = BuyStrategyConverter.class)
    private BuyStrategyType type;
    @Column(name = "target_value")
    private String targetValue;
    @Column(name = "leverage")
    private int leverage;
    @Column(name = "order_balance_ratio")
    private double orderBalanceRatio;

    public static BuyStrategyEntity create(BuyStrategyType selectedBuyStrategyType, int leverage, double orderBalanceRatio, String riskRewardStrategyString) {
        BuyStrategyEntity entity = new BuyStrategyEntity();
        entity.type = selectedBuyStrategyType;
        entity.leverage = leverage;
        entity.orderBalanceRatio = orderBalanceRatio;
        entity.targetValue = riskRewardStrategyString;
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
