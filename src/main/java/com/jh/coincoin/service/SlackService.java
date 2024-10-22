package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.indicator.IndicatorService;
import com.jh.coincoin.model.Slack.Event;
import com.jh.coincoin.service.slack.ActionHandler;
import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final AdminService adminService;
    private final Slack slackClient = Slack.getInstance();
    private Map<String, IndicatorService> indicatorServiceMap;
    private Map<Command, ActionHandler> actionHandlerMap;
    private final String webHookURL;

    @Autowired
    public void setIndicatorServiceMap(Set<IndicatorService> indicatorServiceSet) {
        this.indicatorServiceMap = indicatorServiceSet.stream().collect(Collectors.toMap(IndicatorService::getName, Function.identity()));
    }

    @Autowired
    public void setActionHandlerMap(Set<ActionHandler> actionHandlerSet) {
        this.actionHandlerMap = actionHandlerSet.stream().collect(Collectors.toMap(ActionHandler::getCommand, Function.identity()));
    }

    public void sendAlert() {
        Interval interval = adminService.getInterval();
        if (!timeChecker(interval))
            return;

        List<String> indicatorNameList = adminService.getTrackingIndicatorNameList();
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        Map<String, String> messages = new HashMap<>();
        for (String name : indicatorNameList) {
            for (Symbol symbol : symbolList) {
                IndicatorService indicator = indicatorServiceMap.get(name);
                String result = indicator.getLastFigure(symbol, interval);

                if (adminService.isDetect(name, result)) {
                    String message = indicator.wrappingMessage(symbol, result);
                    messages.put(name, message);
                }
            }
        }

        if (messages.size() != 0)
            sendMessage("지표 감지", messages);
    }

    public void handleAction(Event event) {
        Map<String, String> resContext = new HashMap<>();
        Pair<Command, List<String>> command = analyzeCommand(event.getText());
        if (command == null) {
            resContext.put("에러", "잘못된 커맨드");
            sendMessage(resContext);
            return;
        }

        ActionHandler actionHandler = actionHandlerMap.get(command.getKey());
        resContext = actionHandler.doAction(command.getValue());

        sendMessage(resContext);
    }

    private void sendMessage(String title, Map<String, String> data){
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

    private void sendMessage(Map<String, String> data){
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

    private boolean timeChecker(Interval interval) {
        int minute = LocalDateTime.now().getMinute();

        if (interval == Interval.ONE_MINUTE)
            return true;

        if (interval == Interval.HOUR && minute == 0)
            return true;

        return minute / interval.getMinute() == 0;
    }

    private Pair<Command, List<String>> analyzeCommand(String rawCommand) {
        String[] splitCommand = rawCommand.split(" ");

        String command = splitCommand[1];
        Command action = Command.of(command);
        if (action == null) // 없는 명령어
            return null;

        if (action != Command.HELP && splitCommand.length < 3) // 명령어 길이 부족
            return null;

        List<String> contextList = new ArrayList<>(Arrays.asList(splitCommand).subList(2, splitCommand.length));

        return Pair.of(action, contextList);
    }
}
