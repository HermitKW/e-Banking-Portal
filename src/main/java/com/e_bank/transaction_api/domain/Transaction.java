package com.e_bank.transaction_api.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A transaction consumed from the Kafka topic. Amount is signed minor units.
 * No customerId: ownership is resolved from the IBAN at query time.
 */
public record Transaction(
        String id,
        long amountMinorUnits,
        String currency,        // ISO 4217, e.g. "GBP"
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

    public Money amount() {
        return Money.of(amountMinorUnits, currency);
    }

    public TransactionType type() {
        return TransactionType.fromSignedMinorUnits(amountMinorUnits);
    }
}
