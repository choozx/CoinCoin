package com.jh.coincoin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final SlackMessageService slackMessageService;

    public void sendAlert(String req) {
        slackMessageService.sendMessage(req);
    }
}
