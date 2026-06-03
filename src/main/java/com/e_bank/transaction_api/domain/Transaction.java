package com.e_bank.transaction_api.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A money-account transaction as consumed from the Kafka {@code transactions} topic.
 *
 * <p>Mirrors the attributes defined by the assessment: a unique id, a signed amount with
 * currency, the account IBAN, a value date and a free-text description. The amount is held as
 * signed integer minor units (negative = debit, positive = credit); see {@link Money}.</p>
 *
 * <p>There is deliberately <strong>no {@code customerId}</strong> here: the source event only
 * carries the IBAN. Ownership (customer → IBANs) is resolved at query time via the account
 * ownership service, so a transaction is linked to a customer through its IBAN.</p>
 */
public record Transaction(
        String id,
        long amountMinorUnits,
        String currency,        // ISO 4217 code, e.g. "GBP"
        String iban,
        LocalDate valueDate,
        String description
) {
    public Transaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(iban, "iban must not be null");
        Objects.requireNonNull(valueDate, "valueDate must not be null");
    }

    /** The signed amount as a {@link Money} value object. */
    public Money amount() {
        return Money.of(amountMinorUnits, currency);
    }

    /** {@link TransactionType#CREDIT} or {@link TransactionType#DEBIT}, derived from the amount sign. */
    public TransactionType type() {
        return TransactionType.fromSignedMinorUnits(amountMinorUnits);
    }
}
