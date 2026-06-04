package com.e_bank.transaction_api.fx;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Live FX client backed by the Frankfurter API (ECB reference rates).
 * Active only under the {@code real-fx} profile; tests/CI use the default {@link ExchangeRateClient}.
 */
@Component
@Profile("real-fx")
public class FrankfurterExchangeRateClient extends AbstractExchangeRateClient {

    private final RestClient restClient;

    public FrankfurterExchangeRateClient(ExchangeRateProperties properties) {
        super(properties.cacheTtl());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    @Override
    protected BigDecimal fetchRate(String from, String to) {
        FrankfurterResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/latest")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .retrieve()
                .body(FrankfurterResponse.class);
        if (response == null || response.rates() == null || response.rates().get(to) == null) {
            throw new IllegalStateException("No rate for %s->%s from Frankfurter".formatted(from, to));
        }
        return response.rates().get(to);
    }
}
