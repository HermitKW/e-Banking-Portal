package com.e_bank.transaction_api.fx;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Default FX client: calls the provider contract {@code GET /rate?from=&to=} (the shape the
 * WireMock stub serves in tests). Active unless the {@code real-fx} profile is enabled.
 */
@Component
@Profile("!real-fx")
public class ExchangeRateClient extends AbstractExchangeRateClient {

    private final RestClient restClient;
    private final String apiKey;

    public ExchangeRateClient(ExchangeRateProperties properties) {
        super(properties.cacheTtl());
        this.apiKey = properties.apiKey();
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    @Override
    protected BigDecimal fetchRate(String from, String to) {
        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rate")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build());
        if (apiKey != null && !apiKey.isBlank()) {
            request = request.header("X-API-Key", apiKey);
        }
        FxRateResponse response = request.retrieve().body(FxRateResponse.class);
        if (response == null || response.rate() == null) {
            throw new IllegalStateException("Empty FX response for %s->%s".formatted(from, to));
        }
        return response.rate();
    }
}
