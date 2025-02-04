package com.jh.coincoin.model.type;

import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.CodeEnum;

import java.util.Arrays;

/**
 * Created by dale on 2024-10-18.
 */
public class SlackType {

    public enum SlashCommand implements CodeEnum<Integer> {
        HELP(1, "/help"),
        SET_SYMBOL(2, "/set_symbol"),
        DELETE_SYMBOL(3, "/delete_symbol"),
        NEW_STRATEGY(4, "/new_strategy")
        ;

        private final int code;
        private final String command;

        SlashCommand(int code, String command) {
            this.code = code;
            this.command = command;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return command;
        }

        public static SlashCommand of(String stringCommand) {
            return Arrays.stream(values()).filter(command -> command.command.equals(stringCommand)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 명령어"));
        }
    }

    public enum InteractiveCommand implements CodeEnum<Integer> {
        DECIDE_ORDER_STRATEGY(1, "decide_order_strategy"),
        SUBMIT_NEW_STRATEGY(2, "submit_new_strategy"),
        ;

        private final int code;
        private final String callbackId;

        InteractiveCommand(int code, String callbackId) {
            this.code = code;
            this.callbackId = callbackId;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return callbackId;
        }

        public static InteractiveCommand of(String stringCommand) {
            return Arrays.stream(values()).filter(command -> command.callbackId.equals(stringCommand)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 명령어"));
        }
    }
}
