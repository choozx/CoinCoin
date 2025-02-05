package com.jh.coincoin.service.slack.interactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;

public interface SheetHandler {

    SheetType getType();
    void updateSheet(JsonNode jsonNode);
    void submitSheet(JsonNode jsonNode);
}
