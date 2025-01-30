package com.jh.coincoin.model.type;

import com.jh.coincoin.support.ServerException;
import com.jh.coincoin.util.CodeEnum;

import java.util.Arrays;

/**
 * Created by dale on 2024-10-18.
 */
public class SlackType {

    public enum Command implements CodeEnum<Integer> {
        HELP(1, "/h"),
        SET_SYMBOL(2, "/ss"),
        DELETE_SYMBOL(3, "/ds"),
        NEW_STRATEGY(4, "/new_strategy")
        ;

        private int code;
        private String typing;

        Command(int code, String typing) {
            this.code = code;
            this.typing = typing;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return typing;
        }

        public static Command of(String stringCommand) {
            return Arrays.stream(values()).filter(command -> command.typing.equals(stringCommand)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 명령어"));
        }
    }
}
