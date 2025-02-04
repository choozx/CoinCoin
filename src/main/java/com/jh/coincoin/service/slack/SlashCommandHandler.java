package com.jh.coincoin.service.slack;

import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.webhook.WebhookPayloads.payload;

/**
 * Created by dale on 2024-10-18.
 */

@RequiredArgsConstructor
public abstract class SlashCommandHandler {

    protected final Slack slackClient = Slack.getInstance();
    protected final String webHookURL;
    public abstract SlashCommand getCommand();
    public abstract void doCommand(String triggerId, String parameter);

    protected void sendMessage(Map<String, String> data){
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

    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }
}
