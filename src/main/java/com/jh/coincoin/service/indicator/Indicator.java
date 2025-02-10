package com.jh.coincoin.service.indicator;


import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.IndicatorType;
import com.slack.api.model.block.composition.TextObject;

import java.util.List;

/**
 * Created by dale on 2024-09-11.
 */

public interface Indicator {
    IndicatorType getType();
    Double getLastValue(Symbol symbol, Interval interval);
    TextObject wrappingMessage(Symbol symbol, Double result);
    boolean isDetectLastValue(Double result);
    void update(Symbol symbol, Interval interval);

}
