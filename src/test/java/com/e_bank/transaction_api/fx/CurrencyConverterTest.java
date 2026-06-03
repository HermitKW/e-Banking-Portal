package com.e_bank.transaction_api.fx;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConverterTest {

    private static final Currency CHF = Currency.getInstance("CHF");

    @Test
    void convertsAndScalesToTargetCurrency() {
        // 100.00 GBP * 1.124 = 112.400 -> 112.40 (CHF, 2 dp)
        assertEquals(new BigDecimal("112.40"),
                CurrencyConverter.convert(new BigDecimal("100.00"), new BigDecimal("1.124"), CHF));
    }

    @Test
    void roundsHalfEvenOnTie() {
        assertEquals(new BigDecimal("1.00"),
                CurrencyConverter.convert(new BigDecimal("1.005"), BigDecimal.ONE, CHF));
        assertEquals(new BigDecimal("1.02"),
                CurrencyConverter.convert(new BigDecimal("1.015"), BigDecimal.ONE, CHF));
    }

    @Test
    void preservesDebitSign() {
        assertEquals(new BigDecimal("-56.20"),
                CurrencyConverter.convert(new BigDecimal("-50.00"), new BigDecimal("1.124"), CHF));
    }
}
