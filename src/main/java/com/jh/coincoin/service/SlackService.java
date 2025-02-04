package com.jh.coincoin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.model.type.SlackType.InteractiveCommand;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import com.jh.coincoin.service.slack.InteractiveHandler;
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
    private Map<InteractiveCommand, InteractiveHandler> interactiveHandlerMap;

    private final String webHookURL;

    @Autowired
    public void setSlashCommandMap(Set<SlashCommandHandler> slashCommandHandlerSet) {
        this.slashCommandMap = slashCommandHandlerSet.stream().collect(Collectors.toMap(SlashCommandHandler::getCommand, Function.identity()));
    }

    @Autowired
    public void setInteractiveHandlerMap(Set<InteractiveHandler> interactiveHandlerSet) {
        this.interactiveHandlerMap = interactiveHandlerSet.stream().collect(Collectors.toMap(InteractiveHandler::getCommand, Function.identity()));
    }

    public void handleActionV2(SlashCommand slashCommand, String triggerId, String parameter) {
        /*  다시 처음부터 생각해야겠다.
        *   커맨드 슬레시를 입력하면 고정값들이 들어가는 항목들은 전부 내려준다. slackClient.viewOpen
        *   그리고 고정값이 아닌 선택에 의해 항목이 변하는것들은 항목이 선택되는 이벤트가 발생하도록하여
        *   viewUpdate로 모달은 업데이트한다.
        *   다만 새로운 전략을 만들거나 할때는 미리 만들고 전략을 만들어야 한다.
        */


        SlashCommandHandler handler = slashCommandMap.get(slashCommand);
        handler.doCommand(triggerId, parameter);
    }

    public void handleInteractive(String callbackId, JsonNode jsonNode, String viewId) {

        InteractiveHandler interactiveHandler = interactiveHandlerMap.get(InteractiveCommand.of(callbackId));
        interactiveHandler.handleInteractive(jsonNode, viewId);
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
