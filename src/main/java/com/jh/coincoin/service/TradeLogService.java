package com.jh.coincoin.service;

import com.jh.coincoin.entity.TradeLogEntity;
import com.jh.coincoin.model.Binance.BuyResultDto;
import com.jh.coincoin.model.Binance.TradeLogDto;
import com.jh.coincoin.model.type.BinanceType.OrderState;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.repo.TradeLogRepository;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<TradeLogDto> getActivePositionList() {
        List<TradeLogEntity> entityList = tradeLogRepository.findAllByOrderState(OrderState.NEW);

        return entityList.stream().map(TradeLogDto::to).collect(Collectors.toList());
    }

    public void closePosition(Symbol symbol, double closePrice, double pnl, double fee) {
        Optional<TradeLogEntity> optionalActivePosition = getEntity(symbol, OrderState.NEW);
        if (optionalActivePosition.isEmpty())
            throw new ServerException(ErrorType.COMMON_FAIL, "활동중인 포지션을 찾을 수 없습니다.");

        TradeLogEntity activePosition = optionalActivePosition.get();

        activePosition.close(closePrice, pnl, fee);
        tradeLogRepository.saveAndFlush(activePosition);
    }

    public void loggingPosition(BuyResultDto buyResultDto) {
        TradeLogEntity logEntity = TradeLogEntity.create(buyResultDto);
        tradeLogRepository.saveAndFlush(logEntity);
    }

    public boolean isExistActivePosition(Symbol symbol) {
        Optional<TradeLogEntity> optionalActivePosition = getEntity(symbol, OrderState.NEW);
        return optionalActivePosition.isPresent();
    }

    private Optional<TradeLogEntity> getEntity(Symbol symbol, OrderState orderState) {
        return tradeLogRepository.findFirstBySymbolAndOrderState(symbol, orderState);
    }
}
