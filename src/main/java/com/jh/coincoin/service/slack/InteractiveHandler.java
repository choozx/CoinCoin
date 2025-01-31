package com.jh.coincoin.service.slack;

import com.jh.coincoin.model.type.SlackType.InteractiveCommand;

public interface InteractiveHandler {

    InteractiveCommand getCommand();
    void handleInteractive(String type);
}
