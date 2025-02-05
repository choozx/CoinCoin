package com.jh.coincoin.service.slack.interactive.sheet;


import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.service.slack.interactive.SheetHandler;
import org.springframework.stereotype.Service;

@Service
public class TradeSheet implements SheetHandler {

    @Override
    public SheetType getType() {
        return SheetType.TRADE;
    }

    @Override
    public void updateSheet(String viewId, JsonNode selectedOption) {

    }

    @Override
    public void submitSheet(JsonNode jsonNode) {

    }
}
