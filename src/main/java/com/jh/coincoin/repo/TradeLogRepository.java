package com.jh.coincoin.repo;

import com.jh.coincoin.entity.TradeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by dale on 2025-02-07.
 */
public interface TradeLogRepository extends JpaRepository<TradeLogEntity, Long> {
}
