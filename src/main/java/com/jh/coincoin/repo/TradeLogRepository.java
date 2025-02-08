package com.jh.coincoin.repo;

import com.jh.coincoin.entity.TradeLogEntity;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by dale on 2025-02-07.
 */
public interface TradeLogRepository extends JpaRepository<TradeLogEntity, Long> {

    Optional<TradeLogEntity> findFirstBySymbolAndOrderState(Symbol symbol, OrderState state);
}
