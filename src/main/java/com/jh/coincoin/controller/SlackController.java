package com.jh.coincoin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.consts.SlackConst;
import com.jh.coincoin.model.type.SlackType;
import com.jh.coincoin.service.SlackInteractiveService;
import com.jh.coincoin.service.SlackCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by dale on 2024-09-09.
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class SlackController {

    private final SlackCommandService slackCommandService;
    private final SlackInteractiveService slackInteractiveService;


    @PostMapping("/slack/interactive")
    public void handleInteractive(@RequestParam("payload") String payload) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(payload);
            log.info("json node :{}", jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        slackInteractiveService.handleInteractive(jsonNode);
    }

    @PostMapping("/slack/command")
    public void newStrategy(@RequestParam Map<String, String> params) {
        String command = params.get(SlackConst.COMMAND); // Slash Command (/new_strategy)
        String triggerId = params.get(SlackConst.TRIGGER_ID); // 모달 띄우기 위한 trigger_id
        String parameter = params.get(SlackConst.TEXT);

        log.info("command {} | triggerId {} | parameter {}", command, triggerId, parameter);

        SlackType.SlashCommand slashCommand = SlackType.SlashCommand.of(command);
        slackCommandService.handleActionV2(slashCommand, triggerId, parameter);
    }

}
