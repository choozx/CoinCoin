package com.jh.coincoin.service.strategy.buy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.model.Binance.CancelOpenOrderReq;
import com.jh.coincoin.model.Binance.ModifyLeverageReq;
import com.jh.coincoin.model.Binance.PositionInfoRes;
import com.jh.coincoin.model.Binance.PositionInfoReq;
import com.jh.coincoin.model.Binance.TickerPriceRes;
import com.jh.coincoin.model.Binance.TickerPriceReq;
import com.jh.coincoin.model.Binance.AccountBalanceReq;
import com.jh.coincoin.model.Binance.AccountBalanceRes;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.BackTest.BackTestBuyDto;
import com.jh.coincoin.model.Strategy.RiskRewardStrategy;
import com.jh.coincoin.model.Strategy.PriceCalculatorDto;
import com.jh.coincoin.model.Strategy.BuyParamDto;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.StrategyType.RiskRewardRatioType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.TradeLogService;
import com.jh.coincoin.service.external.BinanceAPIService;
import com.jh.coincoin.service.strategy.buy.calculator.RiskRewardCalculator;
import com.jh.coincoin.util.BinanceUtil;
import com.jh.coincoin.util.CommonUtil;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.NumberInputElement;
import com.slack.api.model.block.element.RadioButtonsElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-11-22.
 * 손절과 익절을 한번에 걸어둠
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class StopAndLimitBuyStrategy implements BuyStrategy {

    private final BinanceAPIService binanceAPIService;
    private final TradeLogService tradeLogService;
    private final SlackMessageService slackMessageService;
    private Map<RiskRewardRatioType, RiskRewardCalculator> riskRewardCalculatorMap;

    @Autowired
    public void setRiskRewardCalculatorMap(Set<RiskRewardCalculator> riskRewardCalculatorSet) {
        this.riskRewardCalculatorMap = riskRewardCalculatorSet.stream().collect(Collectors.toMap(RiskRewardCalculator::getType, Function.identity()));
    }

    @Override
    public BuyStrategyType getType() {
        return BuyStrategyType.STOP_AND_LIMIT;
    }

    @Override
    public PositionInfoRes buy(BuyParamDto buyParamDto) {
        // 1.주문된 상태 체크 redis에서 주문정보 get-> 주문된 상태면 return
        long now = DateTimeUtil.getCurrentTimeMillis();
        Symbol symbol = buyParamDto.getSymbol();

        AccountBalanceReq accountBalanceReq = AccountBalanceReq.builder()
                .timestamp(now)
                .build();

        TickerPriceReq tickerPriceReq = TickerPriceReq.builder()
                .symbol(symbol)
                .build();

        AccountBalanceRes accountBalance = binanceAPIService.getAccountBalance(accountBalanceReq);
        TickerPriceRes tickerPrice = binanceAPIService.getTickerPrice(tickerPriceReq);

        double availableBalance = accountBalance.getAvailableBalance();

        // 레버리지 조정
        int leverage = buyParamDto.getLeverage();
        ModifyLeverageReq modifyLeverageReq = ModifyLeverageReq.builder()
                .symbol(symbol)
                .leverage(leverage)
                .timestamp(now)
                .build();
        binanceAPIService.modifyLeverage(modifyLeverageReq);
        log.info("레버리지 조정 : x{}", leverage);

        // FIXME 추후 MIN_ORDER_AMOUNT는 fapi/v1/exchangeInfo의 min_national 필드값을 참조해서 써야됨
        double orderBalanceRatio = buyParamDto.getOrderBalanceRatio();
        double orderBalance = Math.max(leverage * (orderBalanceRatio * availableBalance), GlobalConst.MIN_ORDER_AMOUNT);

        double quantity = orderBalance / tickerPrice.getPrice();

        // TODO 여기서 quantity가 최소 주문 갯수를 넘지 못하면 slack 알림후 return

        // 최초 주문
        Side side = buyParamDto.getSide();
        NewOrderReq order = NewOrderReq.builder()
                .symbol(symbol)
                .side(side)
                .type(Order.MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .timestamp(now)
                .build();
        log.info("최초 주문 :{}", order);
        binanceAPIService.newOrder(order);

        // 주문 확인
        PositionInfoReq positionInfoReq = PositionInfoReq.builder()
                .symbol(symbol)
                .timestamp(now)
                .build();
        List<PositionInfoRes> positionInfoResList = binanceAPIService.getPositionInfo(positionInfoReq);
        PositionInfoRes positionInfoRes = positionInfoResList.get(0);   // 이 전략의 경우에는 포지션을 하나만 잡을것이기 인덱스 0에서 가져온다
        log.info("주문 정보 확인 : {}", positionInfoRes);

        RiskRewardStrategy riskRewardStrategy;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            riskRewardStrategy = objectMapper.readValue(buyParamDto.getTargetValue(), RiskRewardStrategy.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        RiskRewardCalculator calculator = riskRewardCalculatorMap.get(riskRewardStrategy.getType());

        // 익절가 주문
        double entryPrice = positionInfoRes.getEntryPrice();
        PriceCalculatorDto tkPriceDto = PriceCalculatorDto.builder()
                .symbol(symbol)
                .interval(buyParamDto.getInterval())
                .side(side)
                .order(Order.TAKE_PROFIT_MARKET)
                .entryPrice(entryPrice)
                .riskRewardRatio(riskRewardStrategy.getLimit())
                .build();
        double tkPrice = calculator.calcPrice(tkPriceDto);
        NewOrderReq tkOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.TAKE_PROFIT_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(tkPrice, 2))
                .closePosition(true)
                .timestamp(now)
                .build();
        binanceAPIService.newOrder(tkOrder);

        // 손절가 주문
        PriceCalculatorDto slPriceDto = PriceCalculatorDto.builder()
                .symbol(symbol)
                .interval(buyParamDto.getInterval())
                .side(side)
                .order(Order.STOP_MARKET)
                .entryPrice(entryPrice)
                .riskRewardRatio(riskRewardStrategy.getStop())
                .build();
        double slPrice = calculator.calcPrice(slPriceDto);
        NewOrderReq slOrder = NewOrderReq.builder()
                .symbol(symbol)
                .side(Side.reverse(side))
                .type(Order.STOP_MARKET)
                .quantity(CommonUtil.formatDecimal(quantity, 3))
                .stopPrice(CommonUtil.formatDecimal(slPrice, 2))
                .closePosition(true)
                .timestamp(now)
                .build();
        binanceAPIService.newOrder(slOrder);

        // 주문 내용 슬랙에 전송
        slackMessageService.sendMessage(positionInfoRes.toDescription(side));

        return positionInfoRes;
    }

    @Override
    public BackTestBuyDto backTestBuy(BuyParamDto buyParamDto, Candle entryCandle) {
        double entryPrice = entryCandle.getOpenPrice();
        long entryTime = entryCandle.getOpenTime();

        Symbol symbol = buyParamDto.getSymbol();
        Side side = buyParamDto.getSide();

        RiskRewardStrategy riskRewardStrategy;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            riskRewardStrategy = objectMapper.readValue(buyParamDto.getTargetValue(), RiskRewardStrategy.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        RiskRewardCalculator calculator = riskRewardCalculatorMap.get(riskRewardStrategy.getType());

        // 익절가
        PriceCalculatorDto tkPriceDto = PriceCalculatorDto.builder()
                .symbol(symbol)
                .interval(buyParamDto.getInterval())
                .side(side)
                .order(Order.TAKE_PROFIT_MARKET)
                .entryPrice(entryPrice)
                .riskRewardRatio(riskRewardStrategy.getLimit())
                .build();
        double tkPrice = calculator.calcPriceForBackTest(tkPriceDto, entryTime);

        // 손절가
        PriceCalculatorDto slPriceDto = PriceCalculatorDto.builder()
                .symbol(symbol)
                .interval(buyParamDto.getInterval())
                .side(side)
                .order(Order.STOP_MARKET)
                .entryPrice(entryPrice)
                .riskRewardRatio(riskRewardStrategy.getStop())
                .build();
        double slPrice = calculator.calcPriceForBackTest(slPriceDto, entryTime);

        // TODO 추후 redis에서 marginRatio값 가져오기
        double liquidationPrice = BinanceUtil.calcLiquidationPrice(side, entryPrice, buyParamDto.getLeverage());   // TODO 청산가 계산
        double stopPrice = side == Side.BUY ? Math.max(slPrice, liquidationPrice): Math.min(slPrice, liquidationPrice);
        return BackTestBuyDto.builder()
                .side(side)
                .entryTime(entryCandle.getOpenTime())
                .avgPrice(entryPrice)
                .limitPrice(tkPrice)
                .stopPrice(stopPrice)
                .build();
    }

    @Override
    public List<InputBlock> getInputBlockList() {
        List<InputBlock> inputBlockList = new ArrayList<>();

        List<OptionObject> riskRewardTypeList = new ArrayList<>();
        for (RiskRewardRatioType type : RiskRewardRatioType.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(type.getDescription())
                            .build())
                    .value(type.getKey())
                    .build();
            riskRewardTypeList.add(optionObject);
        }

        InputBlock riskRewardTypeBlock = InputBlock.builder()
                .blockId(SlackConst.RISK_REWARD_TYPE)
                .label(PlainTextObject.builder().text("손익절 비율 타입").build())
                .element(RadioButtonsElement.builder()
                        .actionId(SlackConst.SELECT_RISK_REWARD_TYPE)
                        .options(riskRewardTypeList)
                        .build())
                .build();

        InputBlock limitBlock = InputBlock.builder()
                .blockId(SlackConst.LIMIT)
                .label(PlainTextObject.builder().text("익절 비율").build())
                .element(NumberInputElement.builder()
                        .actionId(SlackConst.SELECT_LIMIT)
                        .minValue("0")
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("비율을 선택하세요").build())
                        .build())
                .build();

        InputBlock stopBlock = InputBlock.builder()
                .blockId(SlackConst.STOP)
                .label(PlainTextObject.builder().text("손절 비율").build())
                .element(NumberInputElement.builder()
                        .actionId(SlackConst.SELECT_STOP)
                        .minValue("0")
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("비율을 선택하세요").build())
                        .build())
                .build();

        inputBlockList.add(riskRewardTypeBlock);
        inputBlockList.add(limitBlock);
        inputBlockList.add(stopBlock);

        return inputBlockList;
    }

    @Override
    public BuyStrategyEntity newEntity(JsonNode decideStrategyValue) {
        BuyStrategyType selectedBuyStrategyType = BuyStrategyType.of(decideStrategyValue.path(SlackConst.BUY_STRATEGY).path(SlackConst.SELECT_BUY_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        int leverage = Integer.parseInt(decideStrategyValue.path(SlackConst.LEVERAGE).path(SlackConst.SELECT_LEVERAGE).path(SlackConst.VALUE).asText());
        double orderBalanceRatio = Double.parseDouble(decideStrategyValue.path(SlackConst.BALANCE_RATIO).path(SlackConst.SELECT_BALANCE_RATIO).path(SlackConst.VALUE).asText()) / 100.0;

        RiskRewardRatioType riskRewardRatioType = RiskRewardRatioType.of(decideStrategyValue.path(SlackConst.RISK_REWARD_TYPE).path(SlackConst.SELECT_RISK_REWARD_TYPE).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        double limit = decideStrategyValue.path(SlackConst.LIMIT).path(SlackConst.SELECT_LIMIT).path(SlackConst.VALUE).asDouble();
        double stop = decideStrategyValue.path(SlackConst.STOP).path(SlackConst.SELECT_STOP).path(SlackConst.VALUE).asDouble();
        RiskRewardStrategy riskRewardStrategy = RiskRewardStrategy.builder()
                .type(riskRewardRatioType)
                .limit(limit)
                .stop(stop)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String riskRewardStrategyString;
        try {
            riskRewardStrategyString = objectMapper.writeValueAsString(riskRewardStrategy);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return BuyStrategyEntity.create(selectedBuyStrategyType, leverage, orderBalanceRatio, riskRewardStrategyString);
    }

    @Override
    public void afterFilled(JsonNode jsonNode) {
        double pnl = jsonNode.path("rp").asDouble();
        if (pnl == 0)   // 단방향 모드이때는 PositionSide가 BOTH로 나와서 주문이 매수인지 매도인지 모름
            return;

        Order order = Order.valueOf(jsonNode.path("ot").asText());
        if (!order.isCloseOrder())
            return;

        Symbol symbol = Symbol.of(jsonNode.path("s").asText());
        double avgPrice = jsonNode.path("ap").asDouble();

        long now = DateTimeUtil.getCurrentTimeMillis();
        CancelOpenOrderReq closeOrder = CancelOpenOrderReq.builder()
                .symbol(symbol)
                .timestamp(now)
                .build();
        binanceAPIService.closeOpenOrder(closeOrder);

        tradeLogService.closePosition(symbol, avgPrice, pnl);

        slackMessageService.sendMessage(String.format("포시션 종료! [%s] pnl:%.3f", symbol, pnl));
    }
}
