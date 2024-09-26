package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.service.indicator.IndicatorService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
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
    private final String webHookURL;

    @Autowired
    public void setIndicatorServiceMap(Set<IndicatorService> indicatorServiceSet) {
        this.indicatorServiceMap = indicatorServiceSet.stream().collect(Collectors.toMap(IndicatorService::getName, Function.identity()));
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

    public void sendMessage(String text, Map<String, String> data){
        try {
            slackClient.send(webHookURL, payload(p -> p
                    .text(text) // 메시지 제목
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
}
