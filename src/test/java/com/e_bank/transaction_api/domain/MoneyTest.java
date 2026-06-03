package com.e_bank.transaction_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the money minor-unit ↔ {@link BigDecimal} conversions. These are the
 * correctness-critical heart of the service, so they are tested as pure functions.
 */
class MoneyTest {

    @Test
    @DisplayName("toDecimal scales by the currency's fraction digits (GBP = 2)")
    void toDecimal_gbp() {
        // 10042 minor units (pence) = GBP 100.42
        assertEquals(new BigDecimal("100.42"), Money.of(10_042, "GBP").toDecimal());
    }

    @Test
    @DisplayName("toDecimal preserves the debit sign")
    void toDecimal_negativeIsDebit() {
        assertEquals(new BigDecimal("-75.00"), Money.of(-7_500, "CHF").toDecimal());
    }

    @Test
    @DisplayName("toDecimal honours zero-fraction-digit currencies (JPY = 0)")
    void toDecimal_jpy() {
        // JPY has no minor unit: 100 minor units = ¥100 (scale 0)
        assertEquals(new BigDecimal("100"), Money.of(100, "JPY").toDecimal());
    }

    @Test
    @DisplayName("toMinorUnits is the exact inverse of toDecimal")
    void toMinorUnits_roundTrip() {
        Currency gbp = Currency.getInstance("GBP");
        assertEquals(10_042, Money.toMinorUnits(new BigDecimal("100.42"), gbp));
        assertEquals(-7_500, Money.toMinorUnits(new BigDecimal("-75.00"), Currency.getInstance("CHF")));
    }

    @Test
    @DisplayName("toMinorUnits applies banker's rounding for sub-minor-unit input")
    void toMinorUnits_bankersRounding() {
        Currency gbp = Currency.getInstance("GBP");
        // 1.005 -> 100.5 minor units -> HALF_EVEN rounds to the even neighbour (100)
        assertEquals(100, Money.toMinorUnits(new BigDecimal("1.005"), gbp));
        // 1.015 -> 101.5 minor units -> HALF_EVEN rounds to the even neighbour (102)
        assertEquals(102, Money.toMinorUnits(new BigDecimal("1.015"), gbp));
    }

    @Test
    @DisplayName("unknown currency code is rejected")
    void of_unknownCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(1, "ZZZ"));
    }
}
