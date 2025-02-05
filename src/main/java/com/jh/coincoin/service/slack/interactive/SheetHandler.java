package com.jh.coincoin.service.slack.interactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;

/** modal 상태를 관리하는 클레스 */
public interface SheetHandler {

    SheetType getType();
    void updateSheet(String viewId, JsonNode selectedOption);
    void submitSheet(JsonNode jsonNode);
}
