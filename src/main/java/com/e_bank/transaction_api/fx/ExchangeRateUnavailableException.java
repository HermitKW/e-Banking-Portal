package com.e_bank.transaction_api.fx;

/** Thrown when the current FX rate cannot be obtained; the request fails closed (no stale totals). */
public class ExchangeRateUnavailableException extends RuntimeException {

    public ExchangeRateUnavailableException(String from, String to, Throwable cause) {
        super("Exchange rate unavailable for %s->%s".formatted(from, to), cause);
    }
}
