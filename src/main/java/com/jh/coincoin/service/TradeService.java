package com.jh.coincoin.service;

import com.jh.coincoin.entity.TradeLogEntity;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.Strategy.BuyParamDto;
import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.repo.TradeLogRepository;
import com.jh.coincoin.service.strategy.StrategyService;
import com.jh.coincoin.service.strategy.buy.BuyStrategy;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-11-22.
 */

@Service
@RequiredArgsConstructor
public class TradeService {

    // 매수를 위한 전략 서버스
    private final StrategyService strategyService;
    private final TradeLogService tradeLogService;
    private final TradeLogRepository tradeLogRepository;

    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;
    private Map<BuyStrategyType, BuyStrategy> buyStrategyMap;

    @Autowired
    public void setOrderStrategyMap(Set<OrderStrategy> orderStrategySet) {
        this.orderStrategyMap = orderStrategySet.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
    }

    @Autowired
    public void setBuyStrategyMap(Set<BuyStrategy> buyStrategySet) {
        this.buyStrategyMap = buyStrategySet.stream().collect(Collectors.toMap(BuyStrategy::getType, Function.identity()));
    }

    /* 코인 하나당 하나의 전략만 가질 수 있음*/
    public void tradeV2() {
        List<TradeStrategyDto> tradeStrategyDtoList = strategyService.getTradeStrategyListByInterval(getMatchingIntervalList());

        for (TradeStrategyDto tradeStrategyDto : tradeStrategyDtoList) {
            OrderStrategyDto orderStrategyDto = tradeStrategyDto.getOrderStrategy();
            Symbol symbol = tradeStrategyDto.getSymbol();

            // 이미 포지션을 잡고있으면 패스
            if (tradeLogService.isExistActivePosition(symbol))
                continue;

            OrderStrategy orderStrategy = orderStrategyMap.get(orderStrategyDto.getType());
            Pair<Boolean, Side> hit = orderStrategy.isHit(tradeStrategyDto.getSymbol(), tradeStrategyDto.getInterval(), orderStrategyDto.getTargetValue());

            if (hit.getLeft()) {
                BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
                BuyStrategyType buyStrategyType = buyStrategyDto.getType();
                BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());

                Side side = hit.getRight();
                BuyParamDto buyParamDto = BuyParamDto.builder()
                        .symbol(tradeStrategyDto.getSymbol())
                        .side(side)
                        .interval(tradeStrategyDto.getInterval())
                        .leverage(buyStrategyDto.getLeverage())
                        .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                        .targetValue(buyStrategyDto.getTargetValue())
                        .build();
                PositionInfoRes positionInfoRes = buyStrategy.order(buyParamDto);   // 새로운 주문 return

                TradeLogEntity logEntity = TradeLogEntity.create(positionInfoRes, side, buyStrategyType);
                tradeLogRepository.saveAndFlush(logEntity);
            }
        }
    }

    private List<Integer> getMatchingIntervalList() {
        int minute = LocalDateTime.now().getMinute();

        List<Integer> targetIntervalList = new ArrayList<>();

        for (Interval interval : Interval.values()) {
            if (minute % interval.getMinute() == 0) {
                targetIntervalList.add(interval.getMinute());
            }
        }
        return targetIntervalList;
    }
}
