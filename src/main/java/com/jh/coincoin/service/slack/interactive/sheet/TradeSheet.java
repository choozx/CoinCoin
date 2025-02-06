package com.jh.coincoin.service.slack.interactive.sheet;


import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.OrderStrategyEntity;
import com.jh.coincoin.entity.TradeStrategyEntity;
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
        Symbol selectedSymbol = Symbol.of(decideStrategy.path("symbol").path("select_symbol").path("selected_option").path("value").asText());
        Interval selectedInterval = Interval.of(decideStrategy.path("interval").path("select_interval").path("selected_option").path("value").asText());
        long selectedOrderStrategyIdx = decideStrategy.path("order_strategy").path("select_order_strategy").path("selected_option").path("value").asLong();
        long selectedBuyStrategyIdx = decideStrategy.path("buy_strategy").path("select_buy_strategy").path("selected_option").path("value").asLong();

        OrderStrategyEntity orderStrategy = orderStrategyRepository.findById(selectedOrderStrategyIdx).orElseThrow(() -> new ServerException(ErrorType.NOT_FOUND_STRATEGY, "진입 전략이 존재하지 않습니다."));
        BuyStrategyEntity buyStrategy = buyStrategyRepository.findById(selectedBuyStrategyIdx).orElseThrow(() -> new ServerException(ErrorType.NOT_FOUND_STRATEGY, "진입 전략이 존재하지 않습니다."));

        TradeStrategyEntity tradeStrategyEntity = TradeStrategyEntity.create(selectedSymbol, selectedInterval, orderStrategy, buyStrategy);

        tradeStrategyRepository.saveAndFlush(tradeStrategyEntity);
    }
}
