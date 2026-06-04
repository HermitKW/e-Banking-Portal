package com.e_bank.transaction_api.fx;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Shared resilience + caching for FX providers: short-TTL cache, retry and circuit breaker,
 * same-currency short-circuit, and fail-closed behaviour (failures propagate, never cached).
 * Concrete providers only implement the HTTP call in {@link #fetchRate}.
 */
public abstract class AbstractExchangeRateClient implements ExchangeRateProvider {

    private final Cache<String, BigDecimal> cache;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    protected AbstractExchangeRateClient(Duration cacheTtl) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(cacheTtl).maximumSize(1_000).build();
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
        return cache.get(fromCurrency + "->" + toCurrency, key -> resilientFetch(fromCurrency, toCurrency));
    }

    private BigDecimal resilientFetch(String from, String to) {
        Supplier<BigDecimal> guarded = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> fetchRate(from, to)));
        try {
            return guarded.get();
        } catch (RuntimeException e) {
            throw new ExchangeRateUnavailableException(from, to, e);
        }
    }

    /** Provider-specific HTTP call returning the current rate, or throwing on failure. */
    protected abstract BigDecimal fetchRate(String fromCurrency, String toCurrency);

    /** A request factory with the configured connect/read timeouts. */
    protected static SimpleClientHttpRequestFactory requestFactory(ExchangeRateProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());
        return factory;
    }
}
