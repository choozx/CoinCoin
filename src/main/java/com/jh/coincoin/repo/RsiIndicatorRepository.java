package com.jh.coincoin.repo;

import com.jh.coincoin.entity.RsiIndicatorEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by dale on 2025-02-10.
 */
public interface RsiIndicatorRepository extends JpaRepository<RsiIndicatorEntity, Long> {

    List<RsiIndicatorEntity> findAllBySymbolAndIntervalAndOpenTimeBetween(Symbol symbol, int interval, long begin, long end);
}
