package com.jh.coincoin.service;

import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.slack.SlashCommandHandler;
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
public class SlackCommandService {

    private Map<SlashCommand, SlashCommandHandler> slashCommandMap;

    @Autowired
    public void setSlashCommandMap(Set<SlashCommandHandler> slashCommandHandlerSet) {
        this.slashCommandMap = slashCommandHandlerSet.stream().collect(Collectors.toMap(SlashCommandHandler::getCommand, Function.identity()));
    }

    public void handleActionV2(SlashCommand slashCommand, String triggerId, String parameter) {
        SlashCommandHandler handler = slashCommandMap.get(slashCommand);
        handler.doCommand(triggerId, parameter);
    }
}
