package com.jh.coincoin.service;

import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.support.ServerException;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.slack.api.webhook.WebhookPayloads.payload;

/**
 * Created by dale on 2024-09-09.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackMessageService {

    @Value("${slack.bot-token}")
    private String botToken;
    private final Slack slackClient = Slack.getInstance();
    private final String webHookURL;

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

    public void sendMessage(Map<String, String> data){
        try {
            slackClient.send(webHookURL, payload(p -> p
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

    public void sendMessage(List<LayoutBlock> layoutBlockList) {
        try {
            slackClient.send(webHookURL, payload(p -> p.blocks(layoutBlockList)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openModal(String triggerId, View modalView) {
        try {
            slackClient.methods(botToken).viewsOpen(r -> r
                    .triggerId(triggerId)
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateModal(String viewId, View modalView) {
        try {
            slackClient.methods(botToken).viewsUpdate(r -> r
                    .viewId(viewId)
                    .view(modalView)
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
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
