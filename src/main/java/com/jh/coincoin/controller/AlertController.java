package com.jh.coincoin.controller;

import com.jh.coincoin.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/redirect/alert")
    public void redirectAlert(@RequestBody String req) {
        alertService.sendAlert(req);
    }
}
