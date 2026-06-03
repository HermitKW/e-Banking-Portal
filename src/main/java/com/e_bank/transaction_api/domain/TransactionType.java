package com.e_bank.transaction_api.domain;

/**
 * Whether a transaction increases ({@link #CREDIT}) or decreases ({@link #DEBIT}) the balance.
 *
 * <p>This is <em>derived</em> from the sign of the signed minor-unit amount, never stored as a
 * separate flag — keeping a single source of truth and matching the signed-amount convention
 * (negative = debit, positive = credit).</p>
 */
public enum TransactionType {
    CREDIT,
    DEBIT;

    /**
     * Classifies a signed minor-unit amount. Zero is treated as a {@link #CREDIT} (a
     * non-negative movement); it contributes nothing to debit totals either way.
     */
    public static TransactionType fromSignedMinorUnits(long minorUnits) {
        return minorUnits < 0 ? DEBIT : CREDIT;
    }
}
