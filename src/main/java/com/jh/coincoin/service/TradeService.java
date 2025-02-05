package com.jh.coincoin.service;

import com.jh.coincoin.model.Strategy.OrderParamDto;
import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
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

            OrderStrategy orderStrategy = orderStrategyMap.get(orderStrategyDto.getType());
            Pair<Boolean, Side> hit = orderStrategy.isHit(tradeStrategyDto.getSymbol(), tradeStrategyDto.getInterval(), orderStrategyDto.getTargetValue());

            if (hit.getLeft()) {
                BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
                BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());

                OrderParamDto orderParamDto = OrderParamDto.builder()
                        .symbol(tradeStrategyDto.getSymbol())
                        .side(hit.getRight())
                        .interval(tradeStrategyDto.getInterval())
                        .leverage(buyStrategyDto.getLeverage())
                        .riskRewardRatioDto(buyStrategyDto.getRiskRewardRatioDto())
                        .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                        .build();
                buyStrategy.order(orderParamDto);
            }
        }
    }

    private List<Integer> getMatchingIntervalList() {
        int minute = LocalDateTime.now().getMinute();

        List<Integer> targetIntervalList = new ArrayList<>();
        targetIntervalList.add(1);  // 인터벌이 1분은 항상 포함되니까

        for (BinanceType.Interval interval : BinanceType.Interval.values()) {
            if (minute % interval.getMinute() == 0) {
                targetIntervalList.add(interval.getMinute());
            }
        }
        return targetIntervalList;
    }
}
