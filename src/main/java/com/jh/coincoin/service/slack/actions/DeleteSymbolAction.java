package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.SlackType.ActionCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.slack.ActionHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-23.
 */

@Service
public class DeleteSymbolAction extends ActionHandler {

    private final AdminService adminService;
    private final CandleService candleService;

    public DeleteSymbolAction(String webHookURL, AdminService adminService, CandleService candleService) {
        super(webHookURL);
        this.adminService = adminService;
        this.candleService = candleService;
    }

    @Override
    public ActionCommand getCommand() {
        return ActionCommand.DELETE_SYMBOL;
    }

    @Override
    public void doAction(List<String> commandContextList) {
        String symbolString = commandContextList.get(0);
        BinanceType.Symbol symbol = BinanceType.Symbol.of(symbolString);

        adminService.deleteSymbol(symbol);
        candleService.removeTrackingCandle(symbol);

        Map<String, String> resText = new HashMap<>();
        resText.put("SYMBOL 제거완료", symbol.getKey());

        sendMessage(resText);
    }
}
