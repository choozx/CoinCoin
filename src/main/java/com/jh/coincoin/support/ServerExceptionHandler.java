package com.jh.coincoin.support;

import com.jh.coincoin.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created by dale on 2024-10-29.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ServerExceptionHandler {

    final SlackService slackService;

    @ExceptionHandler(ServerException.class)
    protected void exceptionHandler(ServerException exception) {
        log.error(ExceptionUtils.getStackTrace(exception));

        slackService.sendMessage(exception.getMsg());
    }
}