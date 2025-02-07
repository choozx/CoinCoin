package com.jh.coincoin.service.indicator;


import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;

/**
 * Created by dale on 2024-09-11.
 */

public interface Indicator {
    IndicatorType getType();
    Double getLastFigure(Symbol symbol, Interval interval);
    String wrappingMessage(Symbol symbol, Double result);
    boolean isDetect(Double result);
    void update(Symbol symbol, Interval interval);

}
