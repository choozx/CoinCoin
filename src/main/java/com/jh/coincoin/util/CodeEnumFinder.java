package com.jh.coincoin.util;

import java.util.Arrays;

/**
 * Created by dale on 2024-09-20.
 */
public class CodeEnumFinder {

    public static <T extends CodeEnum<String>> T findByCode(Class<T> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants()).filter(v -> v.getCode().equalsIgnoreCase(code)).findFirst().orElseThrow(() ->
                new IllegalArgumentException(String.format("Unknown enum:%s, code:%s", enumClass.getSimpleName(), code)));
    }

    public static <T extends CodeEnum<Integer>> T findByCode(Class<T> enumClass, int code) {
        return Arrays.stream(enumClass.getEnumConstants()).filter(v -> v.getCode() == code).findFirst().orElseThrow(() ->
                new IllegalArgumentException(String.format("Unknown enum:%s, code:%s", enumClass.getSimpleName(), code)));
    }
}
