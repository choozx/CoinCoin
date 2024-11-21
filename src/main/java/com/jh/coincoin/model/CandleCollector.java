package com.jh.coincoin.model;

import lombok.Getter;

/**
 * Created by dale on 2024-11-21.
 */
public class CandleCollector {

    @Getter
    public static class TrackingSymbolReq {
        private String name;

        public TrackingSymbolReq(String name) {
            this.name = name;
        }
    }
}
