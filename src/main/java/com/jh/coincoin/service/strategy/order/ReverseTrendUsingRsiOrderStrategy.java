package com.jh.coincoin.service.strategy.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.model.Strategy.RSIValue;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.repo.OrderStrategyRepository;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.NumberInputElement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.jh.coincoin.model.type.StrategyType.OrderStrategyType.REVERSE_TREND_USING_RSI;

/**
 * Created by dale on 2024-11-22.
 * rsi를 이용한 주문전략
 */

@Service
@RequiredArgsConstructor
public class ReverseTrendUsingRsiOrderStrategy implements OrderStrategy {

    private final RSIIndicator rsiIndicator;
    private final OrderStrategyRepository orderStrategyRepository;

    @Override
    public OrderStrategyType getType() {
        return REVERSE_TREND_USING_RSI;
    }

    @Override
    public Pair<Boolean, Side> isHit(Symbol symbol, Interval interval, String targetValue) {
        RSIValue rsiValue = convertToRSI(targetValue);
        Double rsiRatio = rsiIndicator.getLastFigure(symbol, interval);

        if (rsiRatio >= rsiValue.getOverBought())
            return Pair.of(true, Side.SELL);

        if (rsiRatio <= rsiValue.getOverSell())
            return Pair.of(true, Side.BUY);

        return Pair.of(false, null);
    }

    @Override
    public List<InputBlock> getInputBlockList() {
        List<InputBlock> inputBlockList = new ArrayList<>();
        InputBlock overBoughtValueBlock = InputBlock.builder()
                .blockId(SlackConst.OVER_BOUGHT)
                .label(PlainTextObject.builder().text("과매수 RSI 타겟 값 설정").build())
                .element(NumberInputElement.builder()
                        .actionId(SlackConst.SELECT_OVER_BOUGHT)
                        .minValue("0")
                        .maxValue("100")
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("과매수 타켓 rsi값을 설정하세요").build())
                        .build())
                .build();

        InputBlock overSellValueBlock = InputBlock.builder()
                .blockId(SlackConst.OVER_SELL)
                .label(PlainTextObject.builder().text("과매도 RSI 타겟 값 설정").build())
                .element(NumberInputElement.builder()
                        .actionId(SlackConst.SELECT_OVER_SELL)
                        .minValue("0")
                        .maxValue("100")
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("과매도 타켓 rsi값을 설정하세요").build())
                        .build())
                .build();

        inputBlockList.add(overBoughtValueBlock);
        inputBlockList.add(overSellValueBlock);
        return inputBlockList;
    }

    @Override
    @Transactional
    public void save(JsonNode decideStrategy) {
        OrderStrategyType selectedOrderType = OrderStrategyType.of(decideStrategy.path(SlackConst.ORDER_STRATEGY).path(SlackConst.SELECT_ORDER_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        int overBought = Integer.parseInt(decideStrategy.path(SlackConst.OVER_BOUGHT).path(SlackConst.SELECT_OVER_BOUGHT).path(SlackConst.VALUE).asText());
        int overSell = Integer.parseInt(decideStrategy.path(SlackConst.OVER_SELL).path(SlackConst.SELECT_OVER_SELL).path(SlackConst.VALUE).asText());

        ObjectMapper objectMapper = new ObjectMapper();
        RSIValue rsiValue = RSIValue.builder()
                .overBought(overBought)
                .overSell(overSell)
                .build();

        String rsiValueString;
        try {
            rsiValueString = objectMapper.writeValueAsString(rsiValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        OrderStrategyEntity orderStrategyEntity = OrderStrategyEntity.create(selectedOrderType, rsiValueString);

        orderStrategyRepository.saveAndFlush(orderStrategyEntity);
    }

    private RSIValue convertToRSI(String jsonValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        RSIValue rsiValue;
        try {
            rsiValue = objectMapper.readValue(jsonValue, RSIValue.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return rsiValue;
    }
}
