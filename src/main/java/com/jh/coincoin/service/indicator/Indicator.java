package com.jh.coincoin.service.indicator;


import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.repo.jdbc.IndicatorBatchRepository;
import com.jh.coincoin.service.CandleService;
import com.slack.api.model.block.composition.TextObject;
import lombok.RequiredArgsConstructor;

/**
 * Created by dale on 2024-09-11.
 */

@RequiredArgsConstructor
public abstract class Indicator {

    protected final CandleService candleService;
    protected final IndicatorBatchRepository indicatorBatchRepository;

    public abstract IndicatorType getType();
    public abstract TextObject wrappingMessage(Symbol symbol, Interval interval);
    public abstract boolean isDetectLastValue(Symbol symbol, Interval interval);
    public abstract void update(Symbol symbol, Interval interval);

}
