package com.jh.coincoin.repo;

import com.jh.coincoin.entity.IndicatorEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by dale on 2025-02-10.
 */
public interface IndicatorRepository extends JpaRepository<IndicatorEntity, Long> {

    List<IndicatorEntity> findAllByTypeAndSymbolAndIntervalAndOpenTimeBetween(IndicatorType type, Symbol symbol, int interval, long begin, long end);
}
