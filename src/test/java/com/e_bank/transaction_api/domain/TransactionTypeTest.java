package com.e_bank.transaction_api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the signed-amount → {@link TransactionType} classification.
 */
class TransactionTypeTest {

    @Test
    @DisplayName("positive amount is a CREDIT")
    void positiveIsCredit() {
        assertEquals(TransactionType.CREDIT, TransactionType.fromSignedMinorUnits(10_000));
    }

    @Test
    @DisplayName("negative amount is a DEBIT")
    void negativeIsDebit() {
        assertEquals(TransactionType.DEBIT, TransactionType.fromSignedMinorUnits(-10_000));
    }

    @Test
    @DisplayName("zero is treated as a CREDIT (non-negative movement)")
    void zeroIsCredit() {
        assertEquals(TransactionType.CREDIT, TransactionType.fromSignedMinorUnits(0));
    }
}
