package com.jh.coincoin.service;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.Strategy.BackTestBuyDto;
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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                var chunkHitList = orderStrategy.getHitList(tradeStrategyDto.getSymbol(), interval, orderStrategyDto.getTargetValue(), chunkCeilBeginTime, chunkFloorEndTime);
                hitList.addAll(chunkHitList);
                chunkCeilBeginTime += interval.getMinute() * GlobalConst.CHUNK_SIZE;
                chunkFloorEndTime = Math.min(chunkFloorEndTime + interval.getMinute() * GlobalConst.CHUNK_SIZE, floorEndTime);
            }
        });
        orderThread.start();

        // TODO 진입DTO 만드는 쓰레드 따로 만들기
        // TODO 캔들 돌려가면서 진입DTO에 따라 손/익절 코드 만들기 <- 마지막 라인이니까 굳이 쓰레드 따로 안써도 될듯

        BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
        BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());
        Deque<BackTestBuyDto> positionDeque = new ArrayDeque<>();
        Thread buyThread = new Thread(() -> {
            while (orderThread.isAlive() || !hitList.isEmpty()) {
                if (hitList.isEmpty())
                    continue;

                Pair<Long, Side> sidePair = hitList.poll();
                long signalTime = sidePair.getLeft();
                Side side = sidePair.getRight();
//                if (lastPositionCloseTime > signalTime)   // 일단 시그널은 다 넣고 아래 캔들 돌릴때 필터링 하는게 나을듯
//                    continue;

                long entryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(signalTime).plusMinutes(interval.getMinute()));
                BuyParamDto buyParamDto = BuyParamDto.builder()
                        .symbol(tradeStrategyDto.getSymbol())
                        .side(side)
                        .interval(tradeStrategyDto.getInterval())
                        .leverage(buyStrategyDto.getLeverage())
                        .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                        .build();
//                BackTestBuyDto backTestBuyDto = buyStrategy.backTestBuy(buyParamDto, );
//                positionDeque.add(backTestBuyDto);
            }
        });
        buyThread.start();

        // 캔들 불러오기
        double changeBalance = initialBalance;
        long entryTime = positionDeque.peekFirst().getEntryTime();
        while (buyThread.isAlive() || !positionDeque.isEmpty()) {
            BackTestBuyDto backTestBuyDto = positionDeque.poll();
        }

        // 지표 계산이 아직 진행중이고, hitList에 요소가 남아있을때까지
//        long lastPositionCloseTime = 0;
//        while (orderThread.isAlive() || !hitList.isEmpty()) {
//            if (hitList.isEmpty())
//                continue;
//
//            Pair<Long, Side> sidePair = hitList.poll();
//            long signalTime = sidePair.getLeft();
//            Side side = sidePair.getRight();
//            if (lastPositionCloseTime > signalTime)
//                continue;
//
//            long entryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(signalTime).plusMinutes(interval.getMinute()));
//            BuyParamDto buyParamDto = BuyParamDto.builder()
//                    .symbol(tradeStrategyDto.getSymbol())
//                    .side(side)
//                    .interval(tradeStrategyDto.getInterval())
//                    .leverage(buyStrategyDto.getLeverage())
//                    .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
//                    .build();
//            BackTestBuyDto backTestBuyDto = buyStrategy.backTestBuy(buyParamDto, changeBalance, entryTime);
//
//            // 위에 리턴값으로 candle 돌려가면서 익절/손절 계산
//            // 비교는 무조건 1분봉으로 해야겠다. N분봉이면 그 사이에 손익 둘다 찍어 버릴 수 있을것 같음
//            TreeMap<Long, Candle> candleMap = candleService.getCandleMapByBeginToDB(symbol, Interval.ONE_MINUTE, signalTime, 100);
//            boolean isActivePotion = true;
//            double size = backTestBuyDto.getSize();
//            double avgPrice = backTestBuyDto.getAvgPrice();
//            double limitPrice = backTestBuyDto.getLimitPrice();
//            double liquidationPrice = backTestBuyDto.getLiquidationPrice();
//            double stopPrice = backTestBuyDto.getStopPrice();
//            double finalStopPrice = side.equals(Side.BUY) ? Math.max(stopPrice, liquidationPrice) : Math.min(stopPrice, liquidationPrice);
//            double pnl = 0;
//
//            while (isActivePotion) {
//                for (Candle candle : candleMap.values()) {
//                    if (shouldExitTrade(side, candle.getClosePrice(), limitPrice, finalStopPrice)) {
//                        pnl = Math.abs(avgPrice - (side.equals(Side.BUY) ? (candle.getClosePrice() > limitPrice ? limitPrice : finalStopPrice) : (candle.getClosePrice() < limitPrice ? limitPrice : finalStopPrice))) * size;
//
//                        changeBalance += (candle.getClosePrice() > limitPrice) == side.equals(Side.BUY) ? pnl : -pnl;
//                        isActivePotion = false;
//                        lastPositionCloseTime = candle.getOpenTime();
//                        break;
//                    }
//                }
//
//                // 조건을 충족하지 못하면 다음 캔들로
//                signalTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(candleMap.lastKey()).plusMinutes(Interval.ONE_MINUTE.getMinute()));
//                candleMap = candleService.getCandleMapByBeginToDB(symbol, Interval.ONE_MINUTE, signalTime, 100);
//            }
////            while (isActivePotion) {
////                // 캔들 돌려보면서 익절/손절 체크
////                for (var entrySet : candleMap.entrySet()) {
////                    Candle candle = entrySet.getValue();
////
////                    if (side.equals(Side.BUY)) {
////                        if (candle.getClosePrice() > limitPrice) { // 롱 익절
////                            pnl = Math.abs(avgPrice - limitPrice) * size;
////                            initialBalance += pnl;
////                            isActivePotion = false;
////                            lastPositionCloseTime = candle.getOpenTime();
////                            break;
////                        }
////
////                        if (candle.getClosePrice() < finalStopPrice) { // 롱 손절
////                            pnl = Math.abs(avgPrice - finalStopPrice) * size;
////                            initialBalance -= pnl;
////                            isActivePotion = false;
////                            lastPositionCloseTime = candle.getOpenTime();
////                            break;
////                        }
////                    } else {
////                        if (candle.getClosePrice() < limitPrice) { // 숏 익절
////                            pnl = Math.abs(avgPrice - limitPrice) * size;
////                            initialBalance += pnl;
////                            isActivePotion = false;
////                            lastPositionCloseTime = candle.getOpenTime();
////                            break;
////                        }
////
////                        if (candle.getClosePrice() > finalStopPrice) { // 숏 손절
////                            pnl = Math.abs(avgPrice - finalStopPrice) * size;
////                            initialBalance -= pnl;
////                            isActivePotion = false;
////                            lastPositionCloseTime = candle.getOpenTime();
////                            break;
////                        }
////                    }
////                }
////
////                // 조건 도달 못했으면 다음 캔들로
////                entryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(entryTime).plusMinutes(100));
////                candleMap = candleService.getCandleMapByBeginAndCount(symbol, Interval.ONE_MINUTE, entryTime, 100);
////            }
////        }
//        }

        slackMessageService.sendMessage("");
    }

    private boolean shouldExitTrade(Side side, double closePrice, double limitPrice, double finalStopPrice) {
        if (side.equals(Side.BUY)) {
            return closePrice > limitPrice || closePrice < finalStopPrice;
        } else {
            return closePrice < limitPrice || closePrice > finalStopPrice;
        }
    }
}
