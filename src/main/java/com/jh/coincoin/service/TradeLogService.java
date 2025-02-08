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

/**
 * Created by dale on 2025-02-08.
 */

@Service
@RequiredArgsConstructor
public class TradeLogService {

    private final TradeLogRepository tradeLogRepository;

    public TradeLogDto getActivePosition(Symbol symbol) {
        TradeLogEntity entity = getEntity(symbol, OrderState.NEW);
        if (entity == null)
            throw new ServerException(ErrorType.COMMON_FAIL, "활동중인 포지션을 찾을 수 없습니다.");

        return TradeLogDto.to(entity);
    }

    private TradeLogEntity getEntity(Symbol symbol, OrderState orderState) {
        return tradeLogRepository.findFirstBySymbolAndOrderState(symbol, orderState);
    }

    public void closePosition(Symbol symbol, double closePrice, double pnl) {
        TradeLogEntity activePosition = getEntity(symbol, OrderState.NEW);

        activePosition.close(closePrice, pnl);
        tradeLogRepository.saveAndFlush(activePosition);
    }
}
