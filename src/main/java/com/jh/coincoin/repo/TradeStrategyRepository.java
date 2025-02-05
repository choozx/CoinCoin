package com.jh.coincoin.repo;

import com.jh.coincoin.entity.TradeStrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeStrategyRepository extends JpaRepository<TradeStrategyEntity, Long> {

    List<TradeStrategyEntity> findAllByIntervalIn(List<Integer> intervalList);
}
