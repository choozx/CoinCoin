package com.jh.coincoin.service;

import com.jh.coincoin.model.BackTest.PnlDto;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.BackTest.BackTestBuyDto;
import com.jh.coincoin.model.Strategy.BuyParamDto;
import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.strategy.StrategyService;
import com.jh.coincoin.service.strategy.buy.BuyStrategy;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackTestService {

    private final StrategyService strategyService;
    private final SlackMessageService slackMessageService;
    private final CandleService candleService;

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

    public void backTest(int tradeStrategyIdx, long begin, long end, double initialBalance) {
        TradeStrategyDto tradeStrategyDto = strategyService.getTradeStrategy(tradeStrategyIdx);

        Interval interval = tradeStrategyDto.getInterval();
        Symbol symbol = tradeStrategyDto.getSymbol();

        long ceilBeginTime = DateTimeUtil.ceilToInterval(begin, interval.getMinute());
        long floorEndTime = DateTimeUtil.floorToInterval(end, interval.getMinute());

        OrderStrategyDto orderStrategyDto = tradeStrategyDto.getOrderStrategy();
        OrderStrategy orderStrategy = orderStrategyMap.get(orderStrategyDto.getType());

        Deque<Pair<Long, Side>> hitList = new ArrayDeque<>();
        Thread orderThread = new Thread(() -> {
            long chunkCeilBeginTime = ceilBeginTime;
            long chunkFloorEndTime = floorEndTime;

            // 청크 단위로 조져
            while (floorEndTime > chunkCeilBeginTime) {
                log.info("order thread 실행중");
                var chunkHitList = orderStrategy.getHitList(tradeStrategyDto.getSymbol(), interval, orderStrategyDto.getTargetValue(), chunkCeilBeginTime, chunkFloorEndTime);
                hitList.addAll(chunkHitList);
                chunkCeilBeginTime += interval.getMinute() * GlobalConst.CHUNK_SIZE;
                chunkFloorEndTime = Math.min(chunkFloorEndTime + interval.getMinute() * GlobalConst.CHUNK_SIZE, floorEndTime);
            }
        });
        orderThread.start();

        BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
        BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());
        Deque<BackTestBuyDto> positionDeque = new ArrayDeque<>();
        Thread buyThread = new Thread(() -> {
            while (orderThread.isAlive() || !hitList.isEmpty()) {
                log.info("buy thread 실행중");
                if (hitList.isEmpty())
                    continue;

                Pair<Long, Side> sidePair = hitList.poll();
                long signalTime = sidePair.getLeft();
                Side side = sidePair.getRight();

                long entryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(signalTime).plusMinutes(interval.getMinute()));
                var candleMap = candleService.getCandleMap(symbol, interval, entryTime, DateTimeUtil.calcEndTime(entryTime, interval.getMinute(), 1));
                Candle entryCandle = candleMap.lastEntry().getValue();
                BuyParamDto buyParamDto = BuyParamDto.builder()
                        .symbol(tradeStrategyDto.getSymbol())
                        .side(side)
                        .interval(tradeStrategyDto.getInterval())
                        .leverage(buyStrategyDto.getLeverage())
                        .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                        .build();
                BackTestBuyDto backTestBuyDto = buyStrategy.backTestBuy(buyParamDto, entryCandle);
                positionDeque.add(backTestBuyDto);
            }
        });
        buyThread.start();

        // 캔들 불러오기
        double totalBalance = initialBalance;
        int winCount = 0;
        int tradeCount = 0;
        long lastPositionCloseTime = 0;
        List<PnlDto> pnlList = new ArrayList<>();
        while (buyThread.isAlive() || !positionDeque.isEmpty()) {
            if (positionDeque.isEmpty())
                continue;

            BackTestBuyDto backTestBuyDto = positionDeque.poll();
            // 포지션 종료보다 전에 진입조건은 무시한다.
            if (lastPositionCloseTime >= backTestBuyDto.getEntryTime())
                continue;

            long entryTime = backTestBuyDto.getEntryTime();
            boolean isPositionActive = true;
            while (isPositionActive) {
                // 비교를 하려면 캔들은 1분봉으로 보는게 더 정확함.
                TreeMap<Long, Candle> candleMap = candleService.getCandleMapByBeginToDB(symbol, Interval.ONE_MINUTE, entryTime, 100);

                for (var candle : candleMap.entrySet()) {
                    // 캔들을 backTestBuyDto와 비교해서 손익절 계산
                    double closePrice = candle.getValue().getClosePrice();

                    if (backTestBuyDto.isPriceHit(closePrice)) {
                        log.info("손익절 발생!");
                        PnlDto pnlDto = calcPnl(closePrice, backTestBuyDto, totalBalance, buyStrategyDto.getLeverage(), buyStrategyDto.getOrderBalanceRatio());

                        double pnl = pnlDto.getPnl();
                        if (pnl >= 0)
                            winCount++;

                        tradeCount++;
                        totalBalance += pnl;
                        isPositionActive = false;
                        lastPositionCloseTime = candle.getKey();
                        pnlList.add(pnlDto);
                        break;
                    }
                }

                if (isPositionActive)
                    entryTime = DateTimeUtil.calcEndTime(end, Interval.ONE_MINUTE.getMinute(), 100);
            }
        }

        log.info("{} ~ {} 총 수익:{} | 승률:{}", DateTimeUtil.toDateTime(ceilBeginTime), DateTimeUtil.toDateTime(floorEndTime), totalBalance, (double) winCount/tradeCount);
        for (var pnl : pnlList) {
            log.info("pnl : {}", pnl);
        }
//        slackMessageService.sendMessage("");
    }

    private PnlDto calcPnl(double closePrice, BackTestBuyDto backTestBuyDto, double balance, int leverage, double orderBalanceRatio) {
        double avgPrice = backTestBuyDto.getAvgPrice();
        double limitPrice = backTestBuyDto.getLimitPrice();
        double stopPrice = backTestBuyDto.getStopPrice();

        Side side = backTestBuyDto.getLimitPrice() > avgPrice ? Side.BUY : Side.SELL;
        double positionSize = (balance * orderBalanceRatio * leverage) / backTestBuyDto.getAvgPrice(); // 포지션 크기 계산

        double priceDiff = 0;
        double closePosition = 0;
        if (side == Side.BUY) {
            if (closePrice > limitPrice){
                priceDiff = limitPrice - avgPrice;
                closePosition = limitPrice;
            }
            if (closePrice < stopPrice) {
                priceDiff = stopPrice - avgPrice;
                closePosition = stopPrice;
            }
        } else {
            if (closePrice < limitPrice) {
                priceDiff = avgPrice - limitPrice;
                closePosition = limitPrice;
            }
            if (closePrice > stopPrice) {
                priceDiff = avgPrice - stopPrice;
                closePosition = stopPrice;
            }
        }

        double pnl = priceDiff * positionSize; // 손익 계산
        double pnlPercentage = (priceDiff / avgPrice) * 100 * leverage; // 손익률 계산

        return PnlDto.builder()
                .avgPrice(avgPrice)
                .closePrice(closePosition)
                .pnl(pnl)
                .pnlPercentage(pnlPercentage)
                .build();
    }

    private boolean shouldExitTrade(Side side, double closePrice, double limitPrice, double finalStopPrice) {
        if (side.equals(Side.BUY)) {
            return closePrice > limitPrice || closePrice < finalStopPrice;
        } else {
            return closePrice < limitPrice || closePrice > finalStopPrice;
        }
    }
}
