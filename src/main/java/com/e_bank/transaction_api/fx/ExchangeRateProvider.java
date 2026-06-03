package com.e_bank.transaction_api.fx;

import java.math.BigDecimal;

/** Supplies the current FX rate for a currency pair. */
public interface ExchangeRateProvider {

    /** Current rate to multiply a 'from' amount by to get 'to'; throws if unavailable (fail-closed). */
    BigDecimal getRate(String fromCurrency, String toCurrency);
}
