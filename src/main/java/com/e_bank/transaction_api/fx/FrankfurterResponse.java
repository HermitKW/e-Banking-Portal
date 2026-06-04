package com.e_bank.transaction_api.fx;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/** JSON shape returned by the Frankfurter API (e.g. {@code {"base":"GBP","rates":{"CHF":1.06}}}). */
public record FrankfurterResponse(
        @JsonProperty("base") String base,
        @JsonProperty("rates") Map<String, BigDecimal> rates
) {
}
