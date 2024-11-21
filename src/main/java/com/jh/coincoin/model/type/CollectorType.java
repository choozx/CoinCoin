package com.jh.coincoin.model.type;

/**
 * Created by dale on 2024-11-21.
 */
public enum CollectorType {

    COLLECT_START("/symbol/collect-start"),
    ;

    private String url;
    CollectorType(String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
}
