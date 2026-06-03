package com.e_bank.transaction_api.kafka;

import com.e_bank.transaction_api.domain.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/** JSON wire shape of a transaction event; mapped to the domain on ingestion. */
public record TransactionMessage(
        @JsonProperty("id") String id,
        @JsonProperty("amountMinorUnits") long amountMinorUnits,
        @JsonProperty("currency") String currency,
        @JsonProperty("iban") String iban,
        @JsonProperty("valueDate") String valueDate,
        @JsonProperty("description") String description
) {
    public Transaction toDomain() {
        return new Transaction(id, amountMinorUnits, currency, iban,
                LocalDate.parse(valueDate), description == null ? "" : description);
    }
}
