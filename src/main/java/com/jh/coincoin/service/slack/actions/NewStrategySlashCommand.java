package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.SlackType.InteractiveCommand;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dale on 2025-01-29.
 */

@Slf4j
@Service
public class NewStrategySlashCommand extends SlashCommandHandler {

    @Value("${slack.bot-token}")
    private String botToken;

    public NewStrategySlashCommand(String webHookURL) {
        super(webHookURL);
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.NEW_STRATEGY;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
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

        InputBlock entryStrategyBlock = InputBlock.builder()
                .blockId("entry_strategy")
                .label(PlainTextObject.builder().text("진입 전략").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_entry_strategy")
                        .placeholder(PlainTextObject.builder().text("전략을 선택하세요").build())
                        .options(orderStrategyOptionList)
                        .build())
                .build();

        View modalView = Views.view(v -> v
                .type("modal")
                .callbackId(InteractiveCommand.DECIDE_ORDER_STRATEGY.getKey())
                .title(Views.viewTitle(title -> title.type("plain_text").text("새로운 전략")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(Collections.singletonList(entryStrategyBlock))
        );

        try {
            slackClient.methods(botToken).viewsOpen(r -> r
                    .triggerId(triggerId)
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }
}
