package com.jh.coincoin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.coincoin.model.Slack;
import com.jh.coincoin.model.type.SlackType;
import com.jh.coincoin.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final SlackService slackService;

    @PostMapping(value = "/slack/actions", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleActions(@RequestBody Slack.EventReq req){
        log.info(req.toString());

//        if (req.getChallenge() == null) {
//            slackService.handleAction(req.getEvent());
//        }

        return req.getChallenge();
    }

    @PostMapping("/slack/interactive")
    public void handleInteractive(@RequestParam("payload") String payload) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode values = jsonNode.path("view").path("state").path("values");
        String viewId = jsonNode.path("view").path("id").asText();

        // key를 어떤껄 써야되나...
        log.info("viewId : {} | json node :{}", viewId, jsonNode);

        slackService.handleInteractive(jsonNode);
    }

    @PostMapping("/slack/command")
    public void newStrategy(@RequestParam Map<String, String> params) {
        String command = params.get("command"); // Slash Command (/new_strategy)
        String triggerId = params.get("trigger_id"); // 모달 띄우기 위한 trigger_id
        String parameter = params.get("text");

        log.info("command {} | triggerId {} | parameter {}", command, triggerId, parameter);

        SlackType.SlashCommand slashCommand = SlackType.SlashCommand.of(command);
        slackService.handleActionV2(slashCommand, triggerId, parameter);
    }

}
