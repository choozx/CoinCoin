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
        CREATE_TRADE_STRATEGY(4, "/create_trade_strategy"),
        CREATE_ORDER_STRATEGY(5, "/create_order_strategy"),
        CREATE_BUY_STRATEGY(6, "/create_buy_strategy"),
        SWITCH_AUTO_TRADE(7, "/auto_trade"),
        BACK_TEST(8, "/back_test"),
        COLLECT_PAST_CANDLE(9, "/collect_past_candle"),
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

    public enum InteractiveType implements CodeEnum<Integer> {
        BLOCK_ACTIONS(1, "block_actions"),
        VIEW_SUBMISSION(2, "view_submission"),
        ;

        private final int code;
        private final String name;

        InteractiveType(int code, String name) {
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

        public static InteractiveType of(String name) {
            return Arrays.stream(values()).filter(type -> type.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 타입"));
        }
    }

    public enum SheetType implements CodeEnum<Integer> {
        TRADE(1, "trade"),
        ORDER(2, "order"),
        BUY(3, "buy"),
        BACK_TEST(4, "back_test"),
        ;

        private final int code;
        private final String key;

        SheetType(int code, String key) {
            this.code = code;
            this.key = key;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getKey() {
            return key;
        }

        public static SheetType of(String key) {
            return Arrays.stream(values()).filter(type -> type.key.equals(key)).findFirst()
                    .orElseThrow(() -> new ServerException(ErrorType.WRONG_COMMAND, "찾을 수 없는 전략"));
        }
    }
}
