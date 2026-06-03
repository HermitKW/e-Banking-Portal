package com.e_bank.transaction_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/** A money amount as signed integer minor units (negative = debit, positive = credit). */
public record Money(long minorUnits, Currency currency) {

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public static Money of(long minorUnits, String currencyCode) {
        return new Money(minorUnits, Currency.getInstance(currencyCode));
    }

    /** Exact decimal at the API boundary, scaled to the currency's fraction digits. */
    public BigDecimal toDecimal() {
        return BigDecimal.valueOf(minorUnits, currency.getDefaultFractionDigits());
    }

    /** Decimal to minor units; banker's rounding resolves any sub-unit fraction. */
    public static long toMinorUnits(BigDecimal amount, Currency currency) {
        return amount.movePointRight(currency.getDefaultFractionDigits())
                .setScale(0, RoundingMode.HALF_EVEN)
                .longValueExact();
    }
}
