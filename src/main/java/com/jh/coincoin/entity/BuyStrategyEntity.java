package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType.RiskRewardRatioConverter;
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
    @Column(name = "leverage")
    private int leverage;
    @Column(name = "order_balance_ratio")
    private double orderBalanceRatio;
    @Column(name = "risk_reward_ratio_type")
    @Convert(converter = RiskRewardRatioConverter.class)
    private RiskRewardRatioType riskRewardRatioType;
    @Column(name = "stop")
    private double stop;    // 손절비율
    @Column(name = "limit")
    private double limit;   // 익절비율

    @Override
    public Long getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
