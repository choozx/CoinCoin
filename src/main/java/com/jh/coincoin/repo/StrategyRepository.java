package com.jh.coincoin.repo;

import com.jh.coincoin.entity.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategyRepository extends JpaRepository<StrategyEntity, Long> {

    List<StrategyEntity> findAllByIntervalIn(List<Integer> intervalList);
}
