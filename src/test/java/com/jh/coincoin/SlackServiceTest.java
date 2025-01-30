package com.jh.coincoin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Binance.NewOrderReq;
import com.jh.coincoin.model.Slack;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.PositionSide;
import com.jh.coincoin.model.type.BinanceType.Side;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.IndicatorService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.indicator.RSIIndicator;
import com.jh.coincoin.service.slack.actions.NewStrategyAction;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.StaticSelectElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class SlackServiceTest {

    private final NewStrategyAction newStrategyAction;

    @Test
    public void 슬랙에_블락_킷_보내기() {
        newStrategyAction.doAction(new ArrayList<>());
    }
}
