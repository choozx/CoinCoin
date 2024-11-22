package com.jh.coincoin.service.indicator;


import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;

/**
 * Created by dale on 2024-09-11.
 */

public interface Indicator {
    String getName();
    Double getLastFigure(Symbol symbol, Interval interval);
    String wrappingMessage(Symbol symbol, Double result);
    boolean isDetect(Double result);

}
