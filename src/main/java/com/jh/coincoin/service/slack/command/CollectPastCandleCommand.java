package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.external.CandleCollectorAPIService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

import static com.jh.coincoin.model.type.SlackType.SlashCommand.COLLECT_PAST_CANDLE;

@Service
public class CollectPastCandleCommand extends SlashCommandHandler {

    private final CandleCollectorAPIService ccApiService;

    public CollectPastCandleCommand(SlackMessageService slackMessageService, CandleCollectorAPIService ccApiService) {
        super(slackMessageService);
        this.ccApiService = ccApiService;
    }

    @Override
    public SlashCommand getCommand() {
        return COLLECT_PAST_CANDLE;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        Symbol symbol = Symbol.of(parameter);

        ccApiService.startCollectPastCandle(symbol);
    }
}
