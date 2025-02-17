package com.jh.coincoin.model;

import lombok.Getter;

/**
 * Created by dale on 2024-11-21.
 */
public class CandleCollector {

    @Getter
    public static class TrackingSymbolReq {
        private final String name;

        public TrackingSymbolReq(String name) {
            this.name = name;
        }
    }

    @Getter
    public static class CollectSymbolReq {
        private final String name;

        public CollectSymbolReq(String name) {
            this.name = name;
        }
    }
}
