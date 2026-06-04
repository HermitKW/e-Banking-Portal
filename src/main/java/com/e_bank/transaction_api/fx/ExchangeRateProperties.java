package com.e_bank.transaction_api.fx;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** FX client configuration (base URL, timeouts, cache TTL, optional API key). */
@ConfigurationProperties("app.fx")
public record ExchangeRateProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Duration cacheTtl,
        String apiKey
) {
    public ExchangeRateProperties {
        connectTimeout = connectTimeout != null ? connectTimeout : Duration.ofSeconds(2);
        readTimeout = readTimeout != null ? readTimeout : Duration.ofSeconds(2);
        cacheTtl = cacheTtl != null ? cacheTtl : Duration.ofMinutes(5);
    }
}
