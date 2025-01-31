package com.jh.coincoin.service.slack.interactive;

import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.SlackType.InteractiveCommand;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.InteractiveHandler;
import com.jh.coincoin.service.strategy.order.OrderStrategy;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.NumberInputElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecideStrategyInteractive implements InteractiveHandler {

    private final Slack slackClient = Slack.getInstance();
    private final AdminService adminService;

    private Map<OrderStrategyType, OrderStrategy> orderStrategyMap;

    @Autowired
    public void setOrderStrategyMap(List<OrderStrategy> orderStrategyList) {
        this.orderStrategyMap = orderStrategyList.stream().collect(Collectors.toMap(OrderStrategy::getType, Function.identity()));
    }

    @Override
    public InteractiveCommand getCommand() {
        return InteractiveCommand.DECIDE_ORDER_STRATEGY;
    }

    @Override
    public void handleInteractive(String type) {
        List<OptionObject> intervalOptionList = new ArrayList<>();
        for (BinanceType.Interval interval : BinanceType.Interval.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(interval.getName())
                            .build())
                    .value(interval.getName())
                    .build();
            intervalOptionList.add(optionObject);
        }

        List<OptionObject> symbolOptionList = new ArrayList<>();
        List<BinanceType.Symbol> symbolList = adminService.getTrackingSymbolList();
        for (BinanceType.Symbol symbol : symbolList) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(symbol.getName())
                            .build())
                    .value(symbol.getName())
                    .build();
            symbolOptionList.add(optionObject);
        }

        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        HeaderBlock headerBlock = HeaderBlock.builder()
                .blockId("header")
                .text(PlainTextObject.builder().text(type + " 전략 시트").build())
                .build();
        layoutBlockList.add(headerBlock);

        InputBlock intervalBlock = InputBlock.builder()
                .blockId("interval")
                .label(PlainTextObject.builder().text("적용 캔들").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_interval")
                        .placeholder(PlainTextObject.builder().text("캔들을 선택하세요").build())
                        .options(intervalOptionList)
                        .build())
                .build();
        layoutBlockList.add(intervalBlock);

        InputBlock symbolBlock = InputBlock.builder()
                .blockId("symbol")
                .label(PlainTextObject.builder().text("코인 선택").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_symbol")
                        .placeholder(PlainTextObject.builder().text("코인을 선택하세요").build())
                        .options(symbolOptionList)
                        .build())
                .build();
        layoutBlockList.add(symbolBlock);

        InputBlock leverageBlock = InputBlock.builder()
                .blockId("leverage")
                .label(PlainTextObject.builder().text("레버리지 선택").build())
                .element(NumberInputElement.builder()
                        .actionId("select_leverage")
                        .minValue(String.valueOf(GlobalConst.MIN_LEVERAGE))
                        .maxValue(String.valueOf(GlobalConst.MAX_LEVERAGE))
                        .initialValue("10")
                        .decimalAllowed(false)
                        .placeholder(PlainTextObject.builder().text("레버리지를 선택하세요").build())
                        .build())
                .build();
        layoutBlockList.add(leverageBlock);

        InputBlock marginRatioBlock = InputBlock.builder()
                .blockId("margin_ratio")
                .label(PlainTextObject.builder().text("레버리지 선택").build())
                .element(NumberInputElement.builder()
                        .actionId("select_margin_ratio")
                        .minValue(String.valueOf(GlobalConst.MIN_MARGIN_RATIO))
                        .maxValue(String.valueOf(GlobalConst.MAX_MARGIN_RATIO))
                        .decimalAllowed(true)
                        .placeholder(PlainTextObject.builder().text("레버리지를 선택하세요").build())
                        .build())
                .build();
        layoutBlockList.add(marginRatioBlock);

        List<InputBlock> strategyValueBlock = orderStrategyMap.get(OrderStrategyType.valueOf(type)).getTargetValueBlockList();
        layoutBlockList.addAll(strategyValueBlock);

        View modalView = Views.view(v -> v
                .type("modal")
                .callbackId("create_new_strategy")
                .title(Views.viewTitle(title -> title.type("plain_text").text("새로운 전략")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(layoutBlockList)
        );

        try {
            slackClient.methods("").viewsOpen(r -> r
                    .triggerId("create_new_strategy")
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }
}
