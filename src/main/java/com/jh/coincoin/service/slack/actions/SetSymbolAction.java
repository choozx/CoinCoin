package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.slack.ActionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-22.
 */

@Service
@RequiredArgsConstructor
public class SetSymbolAction implements ActionHandler {

    private final AdminService adminService;
    private final CandleCollectorAPIService ccApiService;

    @Override
    public Command getCommand() {
        return Command.SET_SYMBOL;
    }

    @Override
    public Map<String, String> doAction(List<String> commandContextList) {
        String symbolString = commandContextList.get(0);
        Symbol symbol = Symbol.of(symbolString);

        ccApiService.orderTrackingSymbol(symbol);

        adminService.setSymbol(symbol);

        Map<String, String> resText = new HashMap<>();
        resText.put("SYMBOL 추가완료", symbol.getKey());
        return resText;
    }
}
