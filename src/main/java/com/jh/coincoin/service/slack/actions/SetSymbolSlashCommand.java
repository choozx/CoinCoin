package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dale on 2024-10-22.
 */

@Service
public class SetSymbolSlashCommand extends SlashCommandHandler {

    private final AdminService adminService;
    private final CandleCollectorAPIService ccApiService;

    public SetSymbolSlashCommand(String webHookURL, AdminService adminService, CandleCollectorAPIService ccApiService) {
        super(webHookURL);
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

        sendMessage(resText);
    }
}
