package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.ActionHandler;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.webhook.Payload;
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
        List<LayoutBlock> list = new ArrayList<>();

        List<OptionObject> indicatorOptionList = new ArrayList<>();
        for (IndicatorType type : IndicatorType.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(type.toString())
                            .build())
                    .value(type.getKey())
                    .build();
            indicatorOptionList.add(optionObject);
        }

        List<OptionObject> intervalOptionList = new ArrayList<>();
        for (Interval interval: Interval.values()) {
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

        SectionBlock sectionBlock = SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text("새로운 전략")
                        .build())
                .build();

        DividerBlock dividerBlock = DividerBlock.builder()
                .blockId("divider")
                .build();

        InputBlock inputBlock = InputBlock.builder()

                .build();

        list.add(sectionBlock);
        list.add(dividerBlock);
        list.add(inputBlock);

        Payload payload = Payload.builder()
                .text("새로운 전략")
                .blocks(list)
                .build();

        try {
            slackClient.send(webHookURL, payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
