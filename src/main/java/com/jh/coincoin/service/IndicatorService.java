package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.service.indicator.Indicator;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        List<LayoutBlock> layoutBlockList = new ArrayList<>();

        HeaderBlock headerBlock = HeaderBlock.builder()
                .text(PlainTextObject.builder().text("지표 감지").build())
                .build();

        List<SectionBlock> indicatorBlockList = new ArrayList<>();
        for (IndicatorType indicatorType : indicatorList) {
            List<TextObject> resultBlockList = new ArrayList<>();
            for (Symbol symbol : symbolList) {
                Indicator indicator = indicatorServiceMap.get(indicatorType);
                Double result = indicator.getLastFigure(symbol, interval);

                if (indicator.isDetect(result)) {
                    TextObject text = indicator.wrappingMessage(symbol, result);
                    resultBlockList.add(text);
                }
            }

            // 결과값이 존재한다면 add
            if (!resultBlockList.isEmpty()) {
                SectionBlock resultBlock = SectionBlock.builder()
                        .text(MarkdownTextObject.builder().text(String.format("*%s*", indicatorType.getKey())).build())
                        .fields(resultBlockList)
                        .build();
                indicatorBlockList.add(resultBlock);
            }
        }

        layoutBlockList.add(headerBlock);
        layoutBlockList.addAll(indicatorBlockList);

        if (indicatorBlockList.size() != 0)
            slackMessageService.sendMessage(layoutBlockList);
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
