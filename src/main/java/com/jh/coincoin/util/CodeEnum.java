package com.jh.coincoin.util;

/**
 * Created by dale on 2024-09-20.
 */
public interface CodeEnum<T>{
    T getCode();

    String getKey();
    default String getComment() {
        return getKey();
    }
}
