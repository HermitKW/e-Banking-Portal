package com.e_bank.transaction_api.fx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/** Converts a source-currency decimal amount into a target currency at a given rate. */
public final class CurrencyConverter {

    private CurrencyConverter() {
    }

    /** sourceAmount * rate, rounded HALF_EVEN to the target currency's fraction digits. */
    public static BigDecimal convert(BigDecimal sourceAmount, BigDecimal rate, Currency target) {
        return sourceAmount.multiply(rate).setScale(target.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }
}
