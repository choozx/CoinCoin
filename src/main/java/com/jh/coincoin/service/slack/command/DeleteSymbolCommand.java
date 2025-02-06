package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dale on 2024-10-23.
 */

@Service
public class DeleteSymbolCommand extends SlashCommandHandler {

    private final AdminService adminService;
    private final CandleService candleService;

    public DeleteSymbolCommand(SlackMessageService slackMessageService, AdminService adminService, CandleService candleService) {
        super(slackMessageService);
        this.adminService = adminService;
        this.candleService = candleService;
    }


    @Override
    public SlashCommand getCommand() {
        return SlashCommand.DELETE_SYMBOL;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        BinanceType.Symbol symbol = BinanceType.Symbol.of(parameter);

        adminService.deleteSymbol(symbol);
        candleService.removeTrackingCandle(symbol);

        Map<String, String> resText = new HashMap<>();
        resText.put("SYMBOL 제거완료", symbol.getKey());

        slackMessageService.sendMessage(resText);
    }
}
