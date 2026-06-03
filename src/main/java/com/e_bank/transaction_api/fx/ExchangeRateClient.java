package com.e_bank.transaction_api.fx;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Supplier;

/** Fetches the current FX rate via RestClient, with retry, circuit breaker and a short-TTL cache. */
@Component
public class ExchangeRateClient implements ExchangeRateProvider {

    private final RestClient restClient;
    private final Cache<String, BigDecimal> cache;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ExchangeRateClient(ExchangeRateProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .build();
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(properties.cacheTtl())
                .maximumSize(1_000)
                .build();
        this.circuitBreaker = CircuitBreaker.ofDefaults("fx");
        this.retry = Retry.of("fx", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .build());
    }

    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE; // no conversion, no provider call
        }
        // Failures propagate (not cached) so we fail closed rather than serve a stale rate.
        return cache.get(fromCurrency + "->" + toCurrency, key -> fetchResilient(fromCurrency, toCurrency));
    }

    private BigDecimal fetchResilient(String from, String to) {
        Supplier<BigDecimal> guarded = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> fetch(from, to)));
        try {
            return guarded.get();
        } catch (RuntimeException e) {
            throw new ExchangeRateUnavailableException(from, to, e);
        }
    }

    private BigDecimal fetch(String from, String to) {
        FxRateResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rate")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .retrieve()
                .body(FxRateResponse.class);
        if (response == null || response.rate() == null) {
            throw new IllegalStateException("Empty FX response for %s->%s".formatted(from, to));
        }
        return response.rate();
    }

    private static SimpleClientHttpRequestFactory requestFactory(ExchangeRateProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());
        return factory;
    }
}
