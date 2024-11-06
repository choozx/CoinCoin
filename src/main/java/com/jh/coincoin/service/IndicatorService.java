package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType;
import com.jh.coincoin.service.indicator.Indicator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private Map<String, Indicator> indicatorServiceMap;

    @Autowired
    public void setIndicatorServiceMap(Set<Indicator> indicatorSet) {
        this.indicatorServiceMap = indicatorSet.stream().collect(Collectors.toMap(Indicator::getName, Function.identity()));
    }

    public void detectIndicator() {
        BinanceType.Interval interval = adminService.getInterval();
        if (!timeChecker(interval))
            return;

        List<String> indicatorNameList = adminService.getTrackingIndicatorNameList();
        List<BinanceType.Symbol> symbolList = adminService.getTrackingSymbolList();

        Map<String, String> messages = new HashMap<>();
        for (String name : indicatorNameList) {
            for (BinanceType.Symbol symbol : symbolList) {
                Indicator indicator = indicatorServiceMap.get(name);
                String result = indicator.getLastFigure(symbol, interval);

                if (adminService.isDetect(name, result)) {
                    String message = indicator.wrappingMessage(symbol, result);
                    messages.put(name, message);
                }
            }
        }

        if (messages.size() != 0)
            slackService.sendMessage("지표 감지", messages);
    }

    private boolean timeChecker(BinanceType.Interval interval) {
        int minute = LocalDateTime.now().getMinute();

        if (interval == BinanceType.Interval.ONE_MINUTE)
            return true;

        if (interval == BinanceType.Interval.HOUR && minute == 0)
            return true;

        return minute / interval.getMinute() == 0;
    }
}
