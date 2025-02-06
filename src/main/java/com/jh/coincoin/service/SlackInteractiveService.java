package com.jh.coincoin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.SlackType.InteractiveType;
import com.jh.coincoin.service.slack.InteractiveTypeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-09-09.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackInteractiveService {

    private Map<InteractiveType, InteractiveTypeHandler> interactiveHandlerMap;

    @Autowired
    public void setInteractiveHandlerMap(Set<InteractiveTypeHandler> interactiveTypeHandlerSet) {
        this.interactiveHandlerMap = interactiveTypeHandlerSet.stream().collect(Collectors.toMap(InteractiveTypeHandler::getType, Function.identity()));
    }

    public void handleInteractive(JsonNode jsonNode) {
        InteractiveType type = InteractiveType.of(jsonNode.path(SlackConst.TYPE).asText());

        InteractiveTypeHandler interactiveTypeHandler = interactiveHandlerMap.get(type);
        interactiveTypeHandler.handleInteractiveType(jsonNode);
    }
}
