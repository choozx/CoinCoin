package com.jh.coincoin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by dale on 2024-09-09.
 */
@Configuration
public class SlackConfig {

    @Value("${slack.token}")
    private String token;

    @Bean
    public String webHookURL() {
        return "https://hooks.slack.com/services/" + token;
    }
}
