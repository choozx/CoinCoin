package com.jh.coincoin.service.slack.interactive.sheet;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.BackTestService;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        int tradeStrategyIdx = backTestParameter.path("").asInt();
        long begin = 0;
        long end = 0;
        double initBalance = 0;

        backTestService.backTest(tradeStrategyIdx, begin, end, initBalance);
    }
}
