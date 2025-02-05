package com.jh.coincoin.service.strategy.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Strategy.RSI;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.NumberInputElement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

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

    @Override
    public OrderStrategyType getType() {
        return REVERSE_TREND_USING_RSI;
    }

    @Override
    public Pair<Boolean, Side> isHit(Symbol symbol, Interval interval, String targetValue) {
        RSI rsi = convertToRSI(targetValue);
        Double rsiRatio = rsiIndicator.getLastFigure(symbol, interval);

        if (rsiRatio >= rsi.getOverbought())
            return Pair.of(true, Side.SELL);

        if (rsiRatio <= rsi.getOversold())
            return Pair.of(true, Side.BUY);

        return Pair.of(false, null);
    }

    @Override
    public List<InputBlock> getInputBlockList() {
        List<InputBlock> inputBlockList = new ArrayList<>();
        InputBlock overBoughtValueBlock = InputBlock.builder()
                .blockId("over_bought_target_value")
                .label(PlainTextObject.builder().text("과매수 RSI 타겟 값 설정").build())
                .element(NumberInputElement.builder()
                        .actionId("select_over_bought_target_value")
                        .minValue("0")
                        .maxValue("100")
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("과매수 타켓 rsi값을 설정하세요").build())
                        .build())
                .build();

        InputBlock overSellValueBlock = InputBlock.builder()
                .blockId("over_sell_target_value")
                .label(PlainTextObject.builder().text("과매도 RSI 타겟 값 설정").build())
                .element(NumberInputElement.builder()
                        .actionId("select_over_sell_target_value")
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

    private RSI convertToRSI(String jsonValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        RSI rsi;
        try {
            rsi = objectMapper.readValue(jsonValue, RSI.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return rsi;
    }
}
