package com.jh.coincoin.model.type;

/**
 * Created by dale on 2024-10-31.
 */
public enum ErrorType {

    COMMON_FAIL(0),

    WRONG_PARAMETER(100),
    WRONG_COMMAND(101),
    OVERFLOW_CANDLE_COUNT(102),
    ;

    private int code;

    ErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
