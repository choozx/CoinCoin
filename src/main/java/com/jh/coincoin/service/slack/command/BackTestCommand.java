package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.SlackType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.jh.coincoin.service.strategy.StrategyService;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.ConfirmationDialogObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.DatePickerElement;
import com.slack.api.model.block.element.DatetimePickerElement;
import com.slack.api.model.block.element.NumberInputElement;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BackTestCommand extends SlashCommandHandler {

    private final StrategyService strategyService;

    public BackTestCommand(SlackMessageService slackMessageService, StrategyService strategyService) {
        super(slackMessageService);
        this.strategyService = strategyService;
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.BACK_TEST;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        String[] split = parameter.split(" ");
        String action = split[0];   // start, stop

        if (action.equals("start")) {
            slackMessageService.openModal(triggerId, createBackTestView());
        }
    }

    private View createBackTestView() {
        var tradeStrategyList = strategyService.getAllTradeStrategy();

        List<OptionObject> tradeStrategyOptionList = new ArrayList<>();
        for (var tradeStrategy : tradeStrategyList) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder().text(tradeStrategy.toDescription()).build())
                    .value(String.valueOf(tradeStrategy.getIdx()))
                    .build();
            tradeStrategyOptionList.add(optionObject);
        }

        InputBlock beginPeriodBlock = InputBlock.builder()
                .blockId(SlackConst.BEGIN)
                .label(PlainTextObject.builder().text("백테스트 시작 시간").build())
                .element(DatePickerElement.builder()
                        .actionId(SlackConst.SELECT_BEGIN)
                        .build())
                .build();

        InputBlock endPeriodBlock = InputBlock.builder()
                .blockId(SlackConst.END)
                .label(PlainTextObject.builder().text("백테스트 종료 시간").build())
                .element(DatePickerElement.builder()
                        .actionId(SlackConst.SELECT_END)
                        .build())
                .build();

        InputBlock balanceBlock = InputBlock.builder()
                .blockId(SlackConst.INIT_BALANCE)
                .label(PlainTextObject.builder().text("초기 시드 설정").build())
                .element(NumberInputElement.builder()
                        .actionId(SlackConst.SELECT_INIT_BALANCE)
                        .placeholder(PlainTextObject.builder().text("시드를 설정하세요").build())
                        .minValue(String.valueOf(GlobalConst.MIN_BACK_TEST_BALANCE))
                        .maxValue(String.valueOf(GlobalConst.MAX_BACK_TEST_BALANCE))
                        .build())
                .build();

        InputBlock tradeStrategyBlock = InputBlock.builder()
                .blockId(SlackConst.TRADE_STRATEGY)
                .label(PlainTextObject.builder().text("자동 매매 전략").build())
                .element(StaticSelectElement.builder()
                        .actionId(SlackConst.SELECT_TRADE_STRATEGY)
                        .placeholder(PlainTextObject.builder().text("매매 전략을 선택하세요").build())
                        .options(tradeStrategyOptionList)
                        .build())
                .build();

        List<LayoutBlock> layoutBlockList = new ArrayList<>();
        layoutBlockList.add(beginPeriodBlock);
        layoutBlockList.add(endPeriodBlock);
        layoutBlockList.add(balanceBlock);
        layoutBlockList.add(tradeStrategyBlock);

        return Views.view(v -> v
                .type(SlackConst.MODAL)
                .callbackId(SlackType.SheetType.BACK_TEST.getKey())
                .title(Views.viewTitle(title -> title.type("plain_text").text("백테스트 시트")))
                .submit(Views.viewSubmit(submit -> submit.type("plain_text").text("Submit")))
                .close(Views.viewClose(close -> close.type("plain_text").text("Cancel")))
                .blocks(layoutBlockList)
        );
    }
}
