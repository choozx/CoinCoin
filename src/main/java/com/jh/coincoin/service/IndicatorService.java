package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.indicator.Indicator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-10-31.
 */

@Service
@RequiredArgsConstructor
public class IndicatorService {

    private final AdminService adminService;
    private final SlackService slackService;
    private Map<IndicatorType, Indicator> indicatorServiceMap;

    @Autowired
    public void setIndicatorServiceMap(Set<Indicator> indicatorSet) {
        this.indicatorServiceMap = indicatorSet.stream().collect(Collectors.toMap(Indicator::getName, Function.identity()));
    }

    public void detectIndicator(Interval interval) {
        List<IndicatorType> indicatorList = adminService.getTrackingIndicatorList();
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        Map<String, String> messages = new HashMap<>();
        for (IndicatorType indicatorType : indicatorList) {
            for (Symbol symbol : symbolList) {
                Indicator indicator = indicatorServiceMap.get(indicatorType);
                Double result = indicator.getLastFigure(symbol, interval);

                if (indicator.isDetect(result)) {
                    String message = indicator.wrappingMessage(symbol, result);
                    messages.put(indicatorType.getKey(), message);
                }
            }
        }

        if (messages.size() != 0)
            slackService.sendMessage("지표 감지", messages);
    }
}
