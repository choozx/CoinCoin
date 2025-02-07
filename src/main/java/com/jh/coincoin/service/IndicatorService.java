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
    private final SlackMessageService slackMessageService;
    private Map<IndicatorType, Indicator> indicatorServiceMap;

    @Autowired
    public void setIndicatorServiceMap(Set<Indicator> indicatorSet) {
        this.indicatorServiceMap = indicatorSet.stream().collect(Collectors.toMap(Indicator::getType, Function.identity()));
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
                    messages.put(indicatorType.getKey(), message);  // FIXME symbol이 여러개 걸린다면 Key값이 겹치겠네
                }
            }
        }

        if (messages.size() != 0)
            slackMessageService.sendMessage("지표 감지", messages);
    }

    /**
     *  여기도 IndicatorSheet라는걸 만들어서 관리할까?
     *  ex) indicator table
     *  | id | type | symbol | interval |
     */
    public void update() {
        List<Symbol> symbolList = adminService.getTrackingSymbolList();

        for (Indicator indicator : indicatorServiceMap.values())
            for (Symbol symbol : symbolList)
                for (Interval interval : Interval.getMatchingIntervalList())
                    indicator.update(symbol, interval);
    }
}
