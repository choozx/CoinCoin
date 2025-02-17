package com.jh.coincoin.model.type;

import lombok.Getter;

/**
 * Created by dale on 2024-11-21.
 */
@Getter
public enum CollectorType {

    COLLECT_START("/symbol/collect-start"),
    COLLECT_PAST_CANDLE("/symbol/collect-past-start");

    private final String url;
    CollectorType(String url) {
        this.url = url;
    }

}
