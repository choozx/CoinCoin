package com.jh.coincoin.support;

import com.jh.coincoin.model.type.ErrorType;
import lombok.Getter;

/**
 * Created by dale on 2024-10-29.
 */


public class ServerException extends RuntimeException {
    @Getter
    protected int code;
    protected String msg;    // 서버용 로깅

    public ServerException(ErrorType errorType, String msg) {
        super(String.format("{SERVER_EXCEPTION:%s, %s}", errorType.getCode(), msg));
        this.code = errorType.getCode();
        this.msg = msg;
    }

    public String getMsg() {
        return msg != null ? msg : null;
    }
}
