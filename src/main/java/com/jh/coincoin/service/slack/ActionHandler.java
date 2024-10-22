package com.jh.coincoin.service.slack;

import com.jh.coincoin.model.type.SlackType.Command;

import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-18.
 */
public interface ActionHandler {

    Command getCommand();
    Map<String, String> doAction(List<String> commandContextList);
}
