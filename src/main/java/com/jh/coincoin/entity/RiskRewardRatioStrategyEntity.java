package com.jh.coincoin.entity;

import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "risk_reward_ratio_strategy")
public class RiskRewardRatioStrategyEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private long idx;
    @Column(name = "type")
    private RiskRewardRatioType type;
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
