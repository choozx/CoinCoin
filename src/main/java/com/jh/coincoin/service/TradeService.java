package com.jh.coincoin.service;

import com.jh.coincoin.model.Binance.BuyResultDto;
import com.jh.coincoin.model.Binance.TradeLogReq;
import com.jh.coincoin.model.Binance.TradeLogRes;
import com.jh.coincoin.model.Binance.PositionInfoReq;
import com.jh.coincoin.model.Binance.TradeLogDto;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.service.external.BinanceAPIService;
import com.jh.coincoin.service.strategy.StrategyService;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    // 매수를 위한 전략 서버스
    private final StrategyService strategyService;
    private final TradeLogService tradeLogService;
    private final BinanceAPIService binanceAPIService;
    private final BuyService buyService;
    private final SlackMessageService slackMessageService;

    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;

    @Autowired
    public void setOrderStrategyMap(Set<OrderStrategy> orderStrategySet) {
        this.orderStrategyMap = orderStrategySet.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
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

            OrderStrategyType orderStrategyType = orderStrategyDto.getType();
            OrderStrategy orderStrategy = orderStrategyMap.get(orderStrategyType);
            Pair<Boolean, Side> hit = orderStrategy.isHit(tradeStrategyDto.getSymbol(), tradeStrategyDto.getInterval(), orderStrategyDto.getTargetValue());

            if (hit.getLeft()) {
                BuyResultDto buyResultDto = buyService.buyPosition(hit.getRight(), tradeStrategyDto);

                tradeLogService.loggingPosition(buyResultDto);
            }
        }
    }

    public void positionCheck() {
        long now = DateTimeUtil.getCurrentTimeMillis();
        List<TradeLogDto> loggingActivePositionList = tradeLogService.getActivePositionList();
        if (loggingActivePositionList.isEmpty())
            return;

        PositionInfoReq positionInfoReq = PositionInfoReq.builder()
                .timestamp(now)
                .build();
        List<PositionInfoRes> activePositionList = binanceAPIService.getPositionInfo(positionInfoReq);

        for (TradeLogDto logDto : loggingActivePositionList) {
            boolean isActive = activePositionList.stream().anyMatch(position -> position.getSymbol().equals(logDto.getSymbol()));

            if (isActive)
                continue;

            // 포지션이 종료된 경우
            Symbol symbol = logDto.getSymbol();
            long startTime = DateTimeUtil.toEpochMilli(logDto.getOpenTime().minusSeconds(5));   // 채결 시간 보정
            TradeLogReq tradeLogReq = TradeLogReq.builder()
                    .symbol(symbol)
                    .startTime(startTime)
                    .timestamp(now)
                    .build();
            log.info("start Time:{}", startTime);
            List<TradeLogRes> tradeLogList = binanceAPIService.getTradeLogList(tradeLogReq);

            // 수익률 계산
            double pnl = 0;
            double fee = 0;
            for (TradeLogRes tradeLog : tradeLogList) {
                pnl += tradeLog.getRealizedPnl();
                fee += tradeLog.getCommission();
            }

            double avgPrice = tradeLogList.stream().filter(log -> !log.getSide().equals(logDto.getSide())).toList().getFirst().getPrice();
            tradeLogService.closePosition(symbol, avgPrice, pnl, fee);

            slackMessageService.sendMessage(String.format("포시션 종료! [%s] pnl:%.3f fee:%.3f 실제 수익:%.3f", symbol, pnl, fee, pnl-fee));
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
