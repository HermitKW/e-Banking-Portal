package com.e_bank.transaction_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * A monetary amount held as a signed integer count of the currency's <em>minor units</em>
 * (cents, pence, rappen, …).
 *
 * <p>Storing money as integer minor units instead of {@code double}/{@code float} eliminates
 * binary floating-point representation error and mirrors how core-banking ledgers actually
 * hold balances. A {@link BigDecimal} is reconstructed only at the API boundary, using the
 * currency's own ISO 4217 fraction-digit count.</p>
 *
 * <p>Sign convention (per the assessment): <strong>negative = debit, positive = credit</strong>.</p>
 */
public record Money(long minorUnits, Currency currency) {

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
    }

    /** Convenience factory from an ISO 4217 currency code (e.g. {@code "GBP"}, {@code "CHF"}). */
    public static Money of(long minorUnits, String currencyCode) {
        return new Money(minorUnits, Currency.getInstance(currencyCode));
    }

    /**
     * Reconstructs the decimal amount at the API boundary, scaled to the currency's fraction
     * digits (e.g. {@code 10042} minor GBP units → {@code 100.42}). This conversion is exact;
     * no rounding occurs.
     */
    public BigDecimal toDecimal() {
        return BigDecimal.valueOf(minorUnits, currency.getDefaultFractionDigits());
    }

    /**
     * Converts a decimal amount into minor units for the given currency — e.g. when parsing an
     * inbound value. Uses banker's rounding (HALF_EVEN) to resolve any sub-minor-unit fraction;
     * the result must fit exactly in a {@code long}.
     */
    public static long toMinorUnits(BigDecimal amount, Currency currency) {
        return amount
                .movePointRight(currency.getDefaultFractionDigits())
                .setScale(0, RoundingMode.HALF_EVEN)
                .longValueExact();
    }
}
