package com.jh.coincoin.service.slack.interactive.sheet;


import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderSheet implements SheetHandler {

    @Value("${slack.bot-token}")
    private final String botToken;
    private final Slack slackClient = Slack.getInstance();
    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;

    @Autowired
    public void setOrderStrategyMap(Set<OrderStrategy> orderStrategySet) {
        this.orderStrategyMap = orderStrategySet.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
    }

    @Override
    public SheetType getType() {
        return SheetType.ORDER;
    }

    @Override
    public void updateSheet(String viewId, JsonNode selectedOptionList) {
        JsonNode selectOption = selectedOptionList.get(0);
        OrderStrategyType selectedOrderType = OrderStrategyType.of(selectOption.path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());

        OptionObject selectedOrderStrategy = OptionObject.builder()
                .text(PlainTextObject.builder()
                        .text(selectedOrderType.getDescription())
                        .build())
                .value(selectedOrderType.getKey())
                .build();

        List<OptionObject> orderStrategyOptionList = new ArrayList<>();
        for (OrderStrategyType type : OrderStrategyType.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(type.getDescription())
                            .build())
                    .value(type.getKey())
                    .build();
            orderStrategyOptionList.add(optionObject);
        }

        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        InputBlock orderStrategyBlock = InputBlock.builder()
                .blockId(SlackConst.ORDER_STRATEGY)
                .label(PlainTextObject.builder().text("진입 전략").build())
                .dispatchAction(true)
                .element(StaticSelectElement.builder()
                        .actionId(SlackConst.SELECT_ORDER_STRATEGY)
                        .placeholder(PlainTextObject.builder().text("진입 전략을 선택하세요").build())
                        .initialOption(selectedOrderStrategy)
                        .options(orderStrategyOptionList)
                        .build())
                .build();

        List<InputBlock> valueBlockList = orderStrategyMap.get(selectedOrderType).getInputBlockList();

        layoutBlockList.add(orderStrategyBlock);
        layoutBlockList.addAll(valueBlockList);

        View modalView = Views.view(v -> v
                .type(SlackConst.MODAL)
                .callbackId(SheetType.ORDER.getKey())
                .title(Views.viewTitle(title -> title.type("plain_text").text("새로운 진입 전략")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(layoutBlockList)
        );

        try {
            slackClient.methods(botToken).viewsUpdate(r -> r
                    .viewId(viewId)
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submitSheet(JsonNode decideStrategy) {
        OrderStrategyType selectedOrderType = OrderStrategyType.of(decideStrategy.path(SlackConst.ORDER_STRATEGY).path(SlackConst.SELECT_ORDER_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asText());
        OrderStrategy orderStrategy = orderStrategyMap.get(selectedOrderType);

        orderStrategy.save(decideStrategy);
    }
}
