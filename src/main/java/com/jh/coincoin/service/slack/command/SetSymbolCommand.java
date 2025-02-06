package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dale on 2024-10-22.
 */

@Service
public class SetSymbolCommand extends SlashCommandHandler {

    private final AdminService adminService;
    private final CandleCollectorAPIService ccApiService;

    public SetSymbolCommand(SlackMessageService slackMessageService, AdminService adminService, CandleCollectorAPIService ccApiService) {
        super(slackMessageService);
        this.adminService = adminService;
        this.ccApiService = ccApiService;
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.SET_SYMBOL;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        Symbol symbol = Symbol.of(parameter);

        ccApiService.orderTrackingSymbol(symbol);

        adminService.setSymbol(symbol);

        Map<String, String> resText = new HashMap<>();
        resText.put("SYMBOL 추가완료", symbol.getKey());

        slackMessageService.sendMessage(resText);
    }
}
