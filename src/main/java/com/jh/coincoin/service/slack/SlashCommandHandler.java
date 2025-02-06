package com.jh.coincoin.service.slack;

import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.SlackMessageService;
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

    protected final SlackMessageService slackMessageService;
    public abstract SlashCommand getCommand();
    public abstract void doCommand(String triggerId, String parameter);
}
