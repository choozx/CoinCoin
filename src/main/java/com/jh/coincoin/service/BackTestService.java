package com.jh.coincoin.service;

import com.jh.coincoin.model.BackTest.BackTestResultDto;
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
import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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

        BlockingQueue<Pair<Long, Side>> hitQueue = new LinkedBlockingQueue<>();
        AtomicBoolean isOrderThreadAlive = new AtomicBoolean(true);
        Thread.startVirtualThread(() -> {
            log.info("order thread 실행중");
            long chunkCeilBeginTime = ceilBeginTime;
            long chunkFloorEndTime = Math.min(DateTimeUtil.calcEndTime(chunkCeilBeginTime, interval.getMinute(), GlobalConst.CHUNK_SIZE), floorEndTime);

            // 청크 단위로 조져
            while (chunkCeilBeginTime < floorEndTime) {
                log.info("진입조건::{}~{}", DateTimeUtil.toDateTime(chunkCeilBeginTime), DateTimeUtil.toDateTime(chunkFloorEndTime));
                var chunkHitList = orderStrategy.getHitList(tradeStrategyDto.getSymbol(), interval, orderStrategyDto.getTargetValue(), chunkCeilBeginTime, chunkFloorEndTime);
                hitQueue.addAll(chunkHitList);
                chunkCeilBeginTime = DateTimeUtil.calcEndTime(chunkCeilBeginTime, interval.getMinute(), GlobalConst.CHUNK_SIZE);
                chunkFloorEndTime = Math.min(DateTimeUtil.calcEndTime(chunkFloorEndTime, interval.getMinute(), GlobalConst.CHUNK_SIZE), floorEndTime);
            }

            log.info("진입 조건 체크 끝!");
            isOrderThreadAlive.set(false);
        });

        BuyStrategyDto buyStrategyDto = tradeStrategyDto.getBuyStrategy();
        BuyStrategy buyStrategy = buyStrategyMap.get(buyStrategyDto.getType());
        BlockingQueue<BackTestBuyDto> positionQueue = new LinkedBlockingQueue<>();
        AtomicBoolean isBuyThreadAlive = new AtomicBoolean(true);
        Thread.startVirtualThread(() -> {
            log.info("buy thread 실행중");
            while (isOrderThreadAlive.get() || !hitQueue.isEmpty()) {
                Pair<Long, Side> sidePair = hitQueue.poll();
                if (sidePair == null)
                    continue;

                long signalTime = sidePair.getLeft();
                log.info("주문! {}", DateTimeUtil.toDateTime(signalTime));
                Side side = sidePair.getRight();

                long entryTime = DateTimeUtil.toEpochMilli(DateTimeUtil.toDateTime(signalTime).plusMinutes(interval.getMinute()));
                var candleMap = candleService.getCandleMapByBeginToDB(symbol, interval, entryTime, 1);
                Candle entryCandle = candleMap.firstEntry().getValue();
                BuyParamDto buyParamDto = BuyParamDto.builder()
                        .symbol(tradeStrategyDto.getSymbol())
                        .side(side)
                        .interval(tradeStrategyDto.getInterval())
                        .leverage(buyStrategyDto.getLeverage())
                        .orderBalanceRatio(buyStrategyDto.getOrderBalanceRatio())
                        .targetValue(buyStrategyDto.getTargetValue())
                        .build();
                BackTestBuyDto backTestBuyDto = buyStrategy.backTestBuy(buyParamDto, entryCandle);
                positionQueue.add(backTestBuyDto);
            }

            isBuyThreadAlive.set(false);
        });

        // 캔들 불러오기
        BackTestResultDto backTestResultDto = new BackTestResultDto(initialBalance);
        long lastPositionCloseTime = 0;
        List<PnlDto> pnlList = new ArrayList<>();
        while (isBuyThreadAlive.get() || !positionQueue.isEmpty()) {
            BackTestBuyDto backTestBuyDto = positionQueue.poll();
            if (backTestBuyDto == null)
                continue;

            // 포지션 종료보다 전에 진입조건은 무시한다.
            if (lastPositionCloseTime >= backTestBuyDto.getEntryTime())
                continue;

            long entryTime = backTestBuyDto.getEntryTime();
            boolean isPositionActive = true;
            TreeMap<Long, Candle> candleMap = new TreeMap<>();
            while (isPositionActive) {
                if (entryTime > end)    // 진입시간이 캔들 끝에 도달했는지 체크
                    break;

                // 비교를 하려면 캔들은 1분봉으로 보는게 더 정확함.
                candleMap.clear();
                candleMap.putAll(candleService.getCandleMapByBeginToDB(symbol, Interval.ONE_MINUTE, entryTime, GlobalConst.CHUNK_SIZE));

                log.info("{}~{} 사이즈:{}", candleMap.firstKey(), candleMap.lastKey(), candleMap.size());
                for (var candle : candleMap.entrySet()) {
                    // 캔들을 backTestBuyDto와 비교해서 손익절 계산
                    double closePrice = candle.getValue().getClosePrice();

                    if (backTestBuyDto.isPriceHit(closePrice)) {
                        log.info("손익절 발생!");
                        PnlDto pnlDto = calcPnl(candle.getValue(), backTestBuyDto, backTestResultDto.getTotalBalance(), buyStrategyDto.getLeverage(), buyStrategyDto.getOrderBalanceRatio());
                        pnlList.add(pnlDto);

                        double pnl = pnlDto.getPnl();
                        backTestResultDto.mergeResult(pnl);

                        isPositionActive = false;
                        lastPositionCloseTime = candle.getKey();
                        break;
                    }
                }

                if (pnlList.size() >= GlobalConst.MAX_STORAGE_PNL_DTO_COUNT) {
                    // 테스트 기간이 길어질수록, pnlList의 메모리 사용량이 늘어남.
                    // pnl 상세는 redis에 저장하고 요약만 slack으로 보내기. 그래서 추후 상세보기 버튼을 만들어 redis에서 읽어오는 식으로 변경하자
                    // ttl은 한시간정도?

                    // TODO redis 저장
                    pnlList.clear();
                }

                if (isPositionActive)
                    entryTime = DateTimeUtil.calcEndTime(entryTime, Interval.ONE_MINUTE.getMinute(), GlobalConst.CHUNK_SIZE);
            }
        }

        if (backTestResultDto.getTradeCount() > 0)
            log.info("{} ~ {} 총 수익:{} | 승률:{}", DateTimeUtil.toDateTime(ceilBeginTime), DateTimeUtil.toDateTime(floorEndTime), backTestResultDto.getTotalBalance(), String.format("%.3f", backTestResultDto.getWinRate()));

        slackMessageService.sendMessage(makeMessageBlock(begin, end, backTestResultDto));
    }

    private PnlDto calcPnl(Candle candle, BackTestBuyDto backTestBuyDto, double balance, int leverage, double orderBalanceRatio) {
        double closePrice = candle.getClosePrice();

        double avgPrice = backTestBuyDto.getAvgPrice();
        double limitPrice = backTestBuyDto.getLimitPrice();
        double stopPrice = backTestBuyDto.getStopPrice();
        double entryBalance = balance * orderBalanceRatio;

        Side side = backTestBuyDto.getSide();
        double positionSize = (entryBalance * leverage) / avgPrice; // 포지션 크기 계산

        double priceDiff = 0;
        double closePositionPrice = 0;
        if (side == Side.BUY) {
            if (closePrice > limitPrice) {
                priceDiff = limitPrice - avgPrice;
                closePositionPrice = limitPrice;
            }
            if (closePrice < stopPrice) {
                priceDiff = stopPrice - avgPrice;
                closePositionPrice = stopPrice;
            }
        } else {
            if (closePrice < limitPrice) {
                priceDiff = avgPrice - limitPrice;
                closePositionPrice = limitPrice;
            }
            if (closePrice > stopPrice) {
                priceDiff = avgPrice - stopPrice;
                closePositionPrice = stopPrice;
            }
        }

        double pnl = priceDiff * positionSize; // 손익 계산
        double pnlPercentage = (pnl / entryBalance) * 100; // 손익률 계산
        double pnlByBalance = (pnl / balance) * 100;

        return PnlDto.builder()
                .openTime(backTestBuyDto.getEntryTime())
                .closeTime(candle.getOpenTime())
                .side(side)
                .avgPrice(avgPrice)
                .closePrice(closePositionPrice)
                .pnl(pnl)
                .pnlPercentage(pnlPercentage)
                .pnlPercentageByBalance(pnlByBalance)
                .build();
    }

    private List<LayoutBlock> makeMessageBlock(long begin, long end, BackTestResultDto backTestResultDto) {
        List<TextObject> summaryBlockList = new ArrayList<>();
        var periodText = MarkdownTextObject.builder().text(String.format("*테스트 기간:%s ~ %s*", DateTimeUtil.toLocalDate(begin), DateTimeUtil.toLocalDate(end))).build();
        var totalPnlText = MarkdownTextObject.builder().text(String.format("*총 수익*: %.5f", backTestResultDto.getTotalBalance())).build();
        var winRateText = MarkdownTextObject.builder().text(String.format("*승률*: %.3f", backTestResultDto.getWinRate())).build();

        summaryBlockList.add(periodText);
        summaryBlockList.add(totalPnlText);
        summaryBlockList.add(winRateText);

        HeaderBlock headerBlock = HeaderBlock.builder()
                .text(PlainTextObject.builder().text("백테스트 결과").build())
                .build();

        ContextBlock contextBlock = ContextBlock.builder()
                .elements(Collections.singletonList(MarkdownTextObject.builder().text("*실제 트레이딩과는 다소 차이가 있을 수 있습니다.*").build()))
                .build();

        SectionBlock sectionBlock = SectionBlock.builder()
                .text(MarkdownTextObject.builder().text("*요약*").build())
                .fields(summaryBlockList)
                .build();

        List<LayoutBlock> layoutBlockList = new ArrayList<>();
        layoutBlockList.add(headerBlock);
        layoutBlockList.add(contextBlock);
        layoutBlockList.add(sectionBlock);

        return layoutBlockList;
    }
}
