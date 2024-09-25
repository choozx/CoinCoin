package com.jh.coincoin.service.indicator;


import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;

/**
 * Created by dale on 2024-09-11.
 */

public interface IndicatorService {
    public String getName();

    public void calc();

    public String getLastFigure(Symbol symbol, Interval interval);

    public String wrappingMessage(Symbol symbol, String result);
}
