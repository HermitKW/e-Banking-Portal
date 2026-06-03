package com.e_bank.transaction_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Shared infrastructure beans. */
@Configuration
public class AppConfig {

    /** Injectable clock so the FX timestamp is deterministic in tests. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
