package com.jh.coincoin.model.consts;

import lombok.Getter;

/**
 * Created by dale on 2025-02-23.
 */

@Getter
public enum ErrorConst {

    ALREADY_RUNNING_PAST_CANDLE_COLLECT(1002)
    ;

    private final int code;

    ErrorConst(int code) {
        this.code = code;
    }
}
