package com.jh.coincoin.controller;

import com.jh.coincoin.model.Slack;
import com.jh.coincoin.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dale on 2024-09-09.
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class SlackController {

    private final SlackService slackService;

//    @PostMapping(value = "/slack/actions", produces = MediaType.APPLICATION_JSON_VALUE)
//    public String handleActions(@RequestBody Slack.EventReq req){
//        log.info(req.toString());
//        return req.getToken();
//    }

    @PostMapping(value = "/slack/actions", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleActions(@RequestBody Slack.TestReq req){
        log.info(req.toString());
        return req.getChallenge();
    }

}
