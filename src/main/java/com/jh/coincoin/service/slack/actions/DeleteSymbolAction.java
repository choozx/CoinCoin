package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.service.slack.ActionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-23.
 */

@Service
@RequiredArgsConstructor
public class DeleteSymbolAction implements ActionHandler {

    private final AdminService adminService;
    private final CandleService candleService;

    @Override
    public Command getCommand() {
        return Command.DELETE_SYMBOL;
    }

    @Override
    public Map<String, String> doAction(List<String> commandContextList) {
        String symbolString = commandContextList.get(0);
        BinanceType.Symbol symbol = BinanceType.Symbol.of(symbolString);

        adminService.deleteSymbol(symbol);
        candleService.removeTrackingCandle(symbol);

        Map<String, String> resText = new HashMap<>();
        resText.put("SYMBOL 제거완료", symbol.getKey());
        return resText;
    }
}
