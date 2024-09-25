package com.jh.coincoin.config;

import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Created by dale on 2024-09-07.
 */
@Configuration
public class RestClientConfig {

    private final String baseUrl = BinanceURL.BASE_URL.getUrl();

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl(baseUrl).build();
    }
}
