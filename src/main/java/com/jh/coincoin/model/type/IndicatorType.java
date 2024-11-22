package com.jh.coincoin.model.type;

import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.CodeEnum;

import java.util.Arrays;

/**
 * Created by dale on 2024-11-22.
 */
public enum IndicatorType implements CodeEnum<Integer> {
    RSI(1, "rsi"),
    ;

    private int code;
    private String name;

    IndicatorType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getKey() {
        return name;
    }

    public static IndicatorType of(int code) {
        return Arrays.stream(values()).filter(indicator -> indicator.code == code).findFirst()
                .orElseThrow(() -> new ServerException(ErrorType.COMMON_FAIL, "등록되지 않은 지표"));
    }
}
