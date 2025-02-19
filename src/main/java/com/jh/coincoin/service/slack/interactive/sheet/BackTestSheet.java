package com.jh.coincoin.service.slack.interactive.sheet;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.BackTestService;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import com.jh.coincoin.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackTestSheet implements SheetHandler {

    private final BackTestService backTestService;

    @Override
    public SheetType getType() {
        return SheetType.BACK_TEST;
    }

    @Override
    public void updateSheet(String viewId, JsonNode selectedOptionList) {

    }

    @Override
    public void submitSheet(JsonNode backTestParameter) {
        // TODO 채워넣기
        log.info("{}", backTestParameter);

        int tradeStrategyIdx = backTestParameter.path(SlackConst.TRADE_STRATEGY).path(SlackConst.SELECT_TRADE_STRATEGY).path(SlackConst.SELECTED_OPTION).path(SlackConst.VALUE).asInt();
        double initBalance = backTestParameter.path(SlackConst.INIT_BALANCE).path(SlackConst.SELECT_INIT_BALANCE).path(SlackConst.VALUE).asDouble();

        String beginString = backTestParameter.path(SlackConst.BEGIN).path(SlackConst.SELECT_BEGIN).path(SlackConst.SELECTED_DATE).asText();
        String endString = backTestParameter.path(SlackConst.END).path(SlackConst.SELECT_END).path(SlackConst.SELECTED_DATE).asText();
        long begin = DateTimeUtil.convertDateStringToEpoch(beginString, DateTimeUtil.YYYY_MM_DD_FMT);
        long end = DateTimeUtil.toEpochMilli(DateTimeUtil.convertDateStringToDateTime(endString, DateTimeUtil.YYYY_MM_DD_FMT).plusDays(1).minusSeconds(1));

        log.info("{}|{}|{} - {}", tradeStrategyIdx, initBalance, DateTimeUtil.toDateTime(begin), DateTimeUtil.toDateTime(end));
//        backTestService.backTest(tradeStrategyIdx, begin, end, initBalance);
    }
}
