package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.NumberInputElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dale on 2025-01-29.
 */

@Slf4j
@Service
public class CreateBuyStrategyCommand extends SlashCommandHandler {

    @Value("${slack.bot-token}")
    private String botToken;

    public CreateBuyStrategyCommand(String webHookURL) {
        super(webHookURL);
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.CREATE_BUY_STRATEGY;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        List<OptionObject> buyStrategyOptionList = new ArrayList<>();
        for (BuyStrategyType type : BuyStrategyType.values()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(type.getDescription())
                            .build())
                    .value(type.getKey())
                    .build();
            buyStrategyOptionList.add(optionObject);
        }

        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        InputBlock buyStrategyBlock = InputBlock.builder()
                .blockId("buy_strategy")
                .label(PlainTextObject.builder().text("매수 전략").build())
                .dispatchAction(true)
                .element(StaticSelectElement.builder()
                        .actionId("select_buy_strategy")
                        .placeholder(PlainTextObject.builder().text("매수 전략을 선택하세요").build())
                        .options(buyStrategyOptionList)
                        .build())
                .build();

        InputBlock leverageBlock = InputBlock.builder()
                .blockId("leverage")
                .label(PlainTextObject.builder().text("레버리지 설정").build())
                .element(NumberInputElement.builder()
                        .actionId("select_leverage")
                        .minValue("0")
                        .maxValue("50")     // 이 값도 코인마다 다름으로 동적으로 처치해줘야됨
                        .decimalAllowed(false)
                        .placeholder(PlainTextObject.builder().text("레버리지를 설정하세요").build())
                        .build())
                .build();

        InputBlock balanceRatioBlock = InputBlock.builder()
                .blockId("balanceRatio")
                .label(PlainTextObject.builder().text("주문 마진 비율").build())
                .element(NumberInputElement.builder()
                        .actionId("select_balanceRatio")
                        .minValue("0")
                        .maxValue("100")
                        .decimalAllowed(false)
                        .placeholder(PlainTextObject.builder().text("한번 진입할때 사용할 마진 비율").build())
                        .build())
                .build();

        layoutBlockList.add(buyStrategyBlock);
        layoutBlockList.add(leverageBlock);
        layoutBlockList.add(balanceRatioBlock);

        View modalView = Views.view(v -> v
                .type("modal")
                .callbackId(SheetType.BUY.getKey())
                .title(Views.viewTitle(title -> title.type("plain_text").text("새로운 전략")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(layoutBlockList)
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
