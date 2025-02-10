package com.jh.coincoin.service.indicator;


import com.jh.coincoin.entity.IndicatorEntity;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.repo.IndicatorRepository;
import com.jh.coincoin.service.CandleService;
import com.jh.coincoin.util.DateTimeUtil;
import com.slack.api.model.block.composition.TextObject;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Created by dale on 2024-09-11.
 */

@RequiredArgsConstructor
public abstract class Indicator {

    protected final CandleService candleService;
    private final IndicatorRepository indicatorRepository;
    public abstract IndicatorType getType();
    public abstract Double getLastValue(Symbol symbol, Interval interval);
    public abstract TextObject wrappingMessage(Symbol symbol, Double result);
    public abstract boolean isDetectLastValue(Double result);
    public abstract void update(Symbol symbol, Interval interval);

    protected List<IndicatorEntity> getIndicatorList(IndicatorType type, Symbol symbol, Interval interval, long begin, long end) {
        long ceil = DateTimeUtil.ceilToInterval(begin, interval.getMinute());
        long floor = DateTimeUtil.floorToInterval(end, interval.getMinute());
        return indicatorRepository.findAllByTypeAndSymbolAndIntervalAndOpenTimeBetween(type, symbol, interval, ceil, floor);
    }

}
