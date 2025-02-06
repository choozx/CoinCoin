package com.jh.coincoin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.model.type.SlackType.InteractiveType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.jh.coincoin.service.slack.InteractiveTypeHandler;
import com.jh.coincoin.support.ServerException;
import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.slack.api.webhook.WebhookPayloads.payload;

/**
 * Created by dale on 2024-09-09.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackService {

    private final Slack slackClient = Slack.getInstance();

    private Map<SlashCommand, SlashCommandHandler> slashCommandMap;
    private Map<InteractiveType, InteractiveTypeHandler> interactiveHandlerMap;

    private final String webHookURL;

    @Autowired
    public void setSlashCommandMap(Set<SlashCommandHandler> slashCommandHandlerSet) {
        this.slashCommandMap = slashCommandHandlerSet.stream().collect(Collectors.toMap(SlashCommandHandler::getCommand, Function.identity()));
    }

    @Autowired
    public void setInteractiveHandlerMap(Set<InteractiveTypeHandler> interactiveTypeHandlerSet) {
        this.interactiveHandlerMap = interactiveTypeHandlerSet.stream().collect(Collectors.toMap(InteractiveTypeHandler::getType, Function.identity()));
    }

    public void handleActionV2(SlashCommand slashCommand, String triggerId, String parameter) {
        SlashCommandHandler handler = slashCommandMap.get(slashCommand);
        handler.doCommand(triggerId, parameter);
    }

    public void handleInteractive(JsonNode jsonNode) {
        InteractiveType type = InteractiveType.of(jsonNode.path(SlackConst.TYPE).asText());

        InteractiveTypeHandler interactiveTypeHandler = interactiveHandlerMap.get(type);
        interactiveTypeHandler.handleInteractiveType(jsonNode);
    }

    public void sendMessage(String title, Map<String, String> data){
        try {
            slackClient.send(webHookURL, payload(p -> p
                    .text(title) // 메시지 제목
                    .attachments(List.of(
                            Attachment.builder()
                                    .fields( // 메시지 본문 내용
                                            data.keySet().stream()
                                                    .map(key -> generateSlackField(key, data.get(key)))
                                                    .collect(Collectors.toList())
                                    ).build())))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String context){
        try {
            slackClient.send(webHookURL, payload(p -> p
                    .text(context)
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }

    private Pair<SlashCommand, List<String>> analyzeCommand(String rawCommand) {
        String[] splitCommand = rawCommand.split(" ");

        String command = splitCommand[1];
        SlashCommand action = SlashCommand.of(command);
        if (action == null) // 없는 명령어
            throw new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 명령어");

        if (action != SlashCommand.HELP && splitCommand.length < 3) // 명령어 길이 부족
            throw new ServerException(ErrorType.WRONG_PARAMETER, "명렁어를 수행하는데 파라미터 부족");

        List<String> contextList = new ArrayList<>(Arrays.asList(splitCommand).subList(2, splitCommand.length));

        return Pair.of(action, contextList);
    }
}
