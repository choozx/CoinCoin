package com.jh.coincoin.service.slack.interactive.sheet;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import org.springframework.stereotype.Service;

@Service
public class RiskRewardRatioSheet implements SheetHandler {

    @Override
    public SheetType getType() {
        return SheetType.RISK_REWARD_RATIO;
    }

    @Override
    public void updateSheet(JsonNode jsonNode) {

    }

    @Override
    public void submitSheet(JsonNode jsonNode) {

    }
}
