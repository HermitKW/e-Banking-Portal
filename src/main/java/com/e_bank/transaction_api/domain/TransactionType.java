package com.e_bank.transaction_api.domain;

/** Credit or debit, derived from the amount sign (never stored as a flag). */
public enum TransactionType {
    CREDIT,
    DEBIT;

    /** Negative is a DEBIT; zero and positive are CREDIT. */
    public static TransactionType fromSignedMinorUnits(long minorUnits) {
        return minorUnits < 0 ? DEBIT : CREDIT;
    }
}
