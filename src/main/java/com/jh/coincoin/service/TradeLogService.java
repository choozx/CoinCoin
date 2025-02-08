package com.jh.coincoin.service;

import com.jh.coincoin.entity.TradeLogEntity;
import com.jh.coincoin.model.Binance.TradeLogDto;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.repo.TradeLogRepository;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by dale on 2025-02-08.
 */

@Service
@RequiredArgsConstructor
public class TradeLogService {

    private final TradeLogRepository tradeLogRepository;

    public TradeLogDto getActivePosition(Symbol symbol) {
        Optional<TradeLogEntity> optionalActivePosition = getEntity(symbol, OrderState.NEW);
        if (optionalActivePosition.isEmpty())
            throw new ServerException(ErrorType.COMMON_FAIL, "활동중인 포지션을 찾을 수 없습니다.");

        return TradeLogDto.to(optionalActivePosition.get());
    }

    public void closePosition(Symbol symbol, double closePrice, double pnl) {
        Optional<TradeLogEntity> optionalActivePosition = getEntity(symbol, OrderState.NEW);
        if (optionalActivePosition.isEmpty())
            throw new ServerException(ErrorType.COMMON_FAIL, "활동중인 포지션을 찾을 수 없습니다.");

        TradeLogEntity activePosition = optionalActivePosition.get();

        activePosition.close(closePrice, pnl);
        tradeLogRepository.saveAndFlush(activePosition);
    }

    public boolean isExistActivePosition(Symbol symbol) {
        Optional<TradeLogEntity> optionalActivePosition = getEntity(symbol, OrderState.NEW);
        return optionalActivePosition.isPresent();
    }

    private Optional<TradeLogEntity> getEntity(Symbol symbol, OrderState orderState) {
        return tradeLogRepository.findFirstBySymbolAndOrderState(symbol, orderState);
    }
}
