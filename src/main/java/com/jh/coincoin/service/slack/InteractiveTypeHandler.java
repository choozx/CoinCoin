package com.jh.coincoin.service.slack;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.InteractiveType;

public interface InteractiveTypeHandler {

    InteractiveType getType();
    void handleInteractiveType(JsonNode jsonNode);
}
