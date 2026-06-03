package com.e_bank.transaction_api.fx;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** JSON shape returned by the external FX provider. */
public record FxRateResponse(
        @JsonProperty("from") String from,
        @JsonProperty("to") String to,
        @JsonProperty("rate") BigDecimal rate
) {
}
