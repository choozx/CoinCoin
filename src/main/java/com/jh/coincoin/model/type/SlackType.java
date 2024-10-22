package com.jh.coincoin.model.type;

import com.jh.coincoin.util.CodeEnum;

import java.util.Arrays;

/**
 * Created by dale on 2024-10-18.
 */
public class SlackType {

    public enum Command implements CodeEnum<Integer> {
        HELP(1, "/h"),
        SET_CRYPTO(2, "/sc"),
        DELETE_CRYPTO(3, "/dc")
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
            return Arrays.stream(values()).filter(command -> command.typing.equals(stringCommand)).findFirst().orElse(null);
        }
    }
}
