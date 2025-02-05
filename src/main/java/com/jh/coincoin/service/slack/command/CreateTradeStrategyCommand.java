package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.jh.coincoin.service.strategy.StrategyService;
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
public class CreateTradeStrategyCommand extends SlashCommandHandler {

    private final AdminService adminService;
    private final StrategyService strategyService;

    @Value("${slack.bot-token}")
    private String botToken;

    public CreateTradeStrategyCommand(String webHookURL, AdminService adminService, StrategyService strategyService) {
        super(webHookURL);
        this.adminService = adminService;
        this.strategyService = strategyService;
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.CREATE_TRADE_STRATEGY;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        List<OptionObject> orderStrategyOptionList = new ArrayList<>();
        for (OrderStrategyDto dto : strategyService.getOrderStrategyList()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(dto.getType().getDescription())
                            .build())
                    .value(String.valueOf(dto.getIdx()))
                    .build();
            orderStrategyOptionList.add(optionObject);
        }

        List<OptionObject> buyStrategyOptionList = new ArrayList<>();
        for (BuyStrategyDto dto : strategyService.getBuyStrategyList()) {
            OptionObject optionObject = OptionObject.builder()
                    .text(PlainTextObject.builder()
                            .text(dto.getType().name())
                            .build())
                    .value(String.valueOf(dto.getIdx()))
                    .build();
            buyStrategyOptionList.add(optionObject);
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

        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        HeaderBlock headerBlock = HeaderBlock.builder()
                .blockId("header")
                .text(PlainTextObject.builder().text("자동 매매 전략 시트").build())
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

        InputBlock intervalBlock = InputBlock.builder()
                .blockId("interval")
                .label(PlainTextObject.builder().text("적용 캔들").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_interval")
                        .placeholder(PlainTextObject.builder().text("캔들을 선택하세요").build())
                        .options(intervalOptionList)
                        .build())
                .build();

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

        InputBlock orderStrategyBlock = InputBlock.builder()
                .blockId("order_strategy")
                .label(PlainTextObject.builder().text("진입 전략").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_order_strategy")
                        .placeholder(PlainTextObject.builder().text("진입 전략을 선택하세요").build())
                        .options(orderStrategyOptionList)
                        .build())
                .build();

        InputBlock buyStrategyBlock = InputBlock.builder()
                .blockId("buy_strategy")
                .label(PlainTextObject.builder().text("매수 전략").build())
                .element(StaticSelectElement.builder()
                        .actionId("select_buy_strategy")
                        .placeholder(PlainTextObject.builder().text("매수 전략을 선택하세요").build())
                        .options(buyStrategyOptionList)
                        .build())
                .build();

        layoutBlockList.add(headerBlock);
        layoutBlockList.add(symbolBlock);
        layoutBlockList.add(intervalBlock);
        layoutBlockList.add(leverageBlock);
        layoutBlockList.add(orderStrategyBlock);
        layoutBlockList.add(buyStrategyBlock);

        View modalView = Views.view(v -> v
                .type("modal")
                .callbackId(SheetType.TRADE.getKey())
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
