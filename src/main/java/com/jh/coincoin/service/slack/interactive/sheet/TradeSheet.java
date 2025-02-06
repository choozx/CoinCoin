package com.jh.coincoin.service.slack.interactive.sheet;


import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.entity.TradeStrategyEntity;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.repo.BuyStrategyRepository;
import com.jh.coincoin.repo.OrderStrategyRepository;
import com.jh.coincoin.repo.TradeStrategyRepository;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeSheet implements SheetHandler {

    private final TradeStrategyRepository tradeStrategyRepository;
    private final OrderStrategyRepository orderStrategyRepository;
    private final BuyStrategyRepository buyStrategyRepository;

    @Override
    public SheetType getType() {
        return SheetType.TRADE;
    }

    @Override
    public void updateSheet(String viewId, JsonNode selectedOptionList) {

    }

    @Override
    @Transactional
    public void submitSheet(JsonNode decideStrategy) {
        Symbol selectedSymbol = Symbol.of(decideStrategy.path(SlackConst.SYMBOL).path(SlackConst.SELECT_SYMBOL).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        Interval selectedInterval = Interval.of(decideStrategy.path(SlackConst.INTERVAL).path(SlackConst.SELECT_INTERVAL).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        long selectedOrderStrategyIdx = decideStrategy.path(SlackConst.ORDER_STRATEGY).path(SlackConst.SELECT_ORDER_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asLong();
        long selectedBuyStrategyIdx = decideStrategy.path(SlackConst.BUY_STRATEGY).path(SlackConst.SELECT_BUY_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asLong();

        OrderStrategyEntity orderStrategy = orderStrategyRepository.findById(selectedOrderStrategyIdx).orElseThrow(() -> new ServerException(ErrorType.NOT_FOUND_STRATEGY, "진입 전략이 존재하지 않습니다."));
        BuyStrategyEntity buyStrategy = buyStrategyRepository.findById(selectedBuyStrategyIdx).orElseThrow(() -> new ServerException(ErrorType.NOT_FOUND_STRATEGY, "진입 전략이 존재하지 않습니다."));

        TradeStrategyEntity tradeStrategyEntity = TradeStrategyEntity.create(selectedSymbol, selectedInterval, orderStrategy, buyStrategy);

        tradeStrategyRepository.saveAndFlush(tradeStrategyEntity);
    }
}
