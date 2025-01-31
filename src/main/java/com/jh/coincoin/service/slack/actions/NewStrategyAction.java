package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.ActionHandler;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dale on 2025-01-29.
 */

@Slf4j
@Service
public class NewStrategyAction extends ActionHandler {

    private final AdminService adminService;

    public NewStrategyAction(String webHookURL, AdminService adminService) {
        super(webHookURL);
        this.adminService = adminService;
    }

    @Override
    public Command getCommand() {
        return Command.NEW_STRATEGY;
    }

    @Override
    public void doAction(List<String> commandContextList) {
        List<OptionObject> orderStrategyOptionList = new ArrayList<>();
        for (OrderStrategyType type : OrderStrategyType.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(type.name())
                            .build())
                    .value(type.name())
                    .build();
            orderStrategyOptionList.add(optionObject);
        }

        List<OptionObject> intervalOptionList = new ArrayList<>();
        for (Interval interval : Interval.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(interval.getName())
                            .build())
                    .value(interval.getName())
                    .build();
            intervalOptionList.add(optionObject);
        }

        List<OptionObject> symbolOptionList = new ArrayList<>();
        List<Symbol> symbolList = adminService.getTrackingSymbolList();
        for (Symbol symbol : symbolList) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(symbol.getName())
                            .build())
                    .value(symbol.getName())
                    .build();
            symbolOptionList.add(optionObject);
        }

        InputBlock entryStrategyBlock = InputBlock.builder()
                .blockId("entry_strategy")
                .label(PlainTextObject.builder().text("진입 전략").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_entry_strategy")
                        .placeholder(PlainTextObject.builder().text("전략을 선택하세요").build())
                        .options(orderStrategyOptionList)
                        .build())
                .build();

        InputBlock intervalBlock = InputBlock.builder()
                .blockId("interval")
                .label(PlainTextObject.builder().text("적용 캔들").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_interval")
                        .placeholder(PlainTextObject.builder().text("캔들을 선택하세요").build())
                        .options(intervalOptionList)
                        .build())
                .build();

        InputBlock symbolBlock = InputBlock.builder()
                .blockId("symbol")
                .label(PlainTextObject.builder().text("코인 선택").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_symbol")
                        .placeholder(PlainTextObject.builder().text("코인을 선택하세요").build())
                        .options(symbolOptionList)
                        .build())
                .build();

        View modalView = Views.view(v -> v
                .type("modal")
                .callbackId("new_strategy")
                .title(Views.viewTitle(title -> title.type("plain_text").text("새로운 전력")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(Arrays.asList(entryStrategyBlock, intervalBlock, symbolBlock))
        );

        try {
            slackClient.methods("").viewsOpen(r -> r
                    .triggerId("triggerId")
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }
}
