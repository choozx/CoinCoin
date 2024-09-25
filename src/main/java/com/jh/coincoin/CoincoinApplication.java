package com.jh.coincoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoincoinApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoincoinApplication.class, args);
    }

}
