package com.jh.coincoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by dale on 2024-09-09.
 */

public class Slack {

    @Data
    public static class TestReq {
        private String token;
        private String challenge;
        private String type;
    }

    @Data
    public static class TestRes {
        private String challenge;
    }

    @Data
    public static class EventReq{
        private String token;
        @JsonProperty("team_id")
        private String teamId;
        @JsonProperty("api_app_id")
        private String apiAppId;
        private Event event;

    }

    @Data
    public static class Event{
        private String user;
        private String type;
        private String text;
    }
}
