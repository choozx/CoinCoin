package com.jh.coincoin;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.slack.command.CreateTradeStrategyCommand;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class SlackTest {

    private final CreateTradeStrategyCommand newStrategyAction;
    private final SlackMessageService slackMessageService;

    @Test
    public void 슬랙에_블락_킷_보내기() {
        newStrategyAction.doCommand("", "");
    }

    @Test
    public void 지표_알람() {
        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        HeaderBlock headerBlock = HeaderBlock.builder()
                .text(PlainTextObject.builder().text("지표 감지").build())
                .build();

        List<SectionBlock> indicatorBlockList = new ArrayList<>();
        List<TextObject> resultBlockList = new ArrayList<>();
        resultBlockList.add(MarkdownTextObject.builder()
                .text(String.format("*%s* : %.2f", BinanceType.Symbol.ETHUSDT, 12.55))
                .build());

        resultBlockList.add(MarkdownTextObject.builder()
                .text(String.format("*%s* : %.2f", BinanceType.Symbol.BTCUSDT, 15.55))
                .build());


        SectionBlock resultBlock = SectionBlock.builder()
                .text(MarkdownTextObject.builder().text(String.format("*[%s]*", IndicatorType.RSI.getKey())).build())
                .fields(resultBlockList)
                .build();

        SectionBlock resultBlock2 = SectionBlock.builder()
                .text(MarkdownTextObject.builder().text(String.format("*[%s]*", IndicatorType.RSI.getKey())).build())
                .fields(resultBlockList)
                .build();

        indicatorBlockList.add(resultBlock);
        indicatorBlockList.add(resultBlock2);

        layoutBlockList.add(headerBlock);
        layoutBlockList.addAll(indicatorBlockList);

        slackMessageService.sendMessage(layoutBlockList);
    }
}
