package com.jh.coincoin.service.slack;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.InteractiveCommand;

public interface InteractiveHandler {

    InteractiveCommand getCommand();
    void handleInteractive(JsonNode jsonNode, String viewId);
}
