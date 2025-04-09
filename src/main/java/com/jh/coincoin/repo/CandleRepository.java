package com.jh.coincoin.repo;

import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by dale on 2024-09-19.
 */
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {

    List<CandleEntity> findAllByOpenTimeBetweenAndSymbol(long openTimeAfter, long openTimeBefore, Symbol symbol);
    List<CandleEntity> findAllBySymbolAndOpenTimeGreaterThanEqualAndOpenTimeLessThan(Symbol symbol, long openTime, long closeTime);
}
