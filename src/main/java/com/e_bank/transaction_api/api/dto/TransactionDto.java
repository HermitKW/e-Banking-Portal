package com.e_bank.transaction_api.api.dto;

import com.e_bank.transaction_api.domain.Transaction;
import com.e_bank.transaction_api.domain.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A single transaction as returned by the API (decimal amounts at the boundary). */
public record TransactionDto(
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amount,
        String currency,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amountInTargetCurrency,
        String iban,
        LocalDate valueDate,
        TransactionType type,
        String description
) {
    public static TransactionDto from(Transaction tx, BigDecimal amountInTargetCurrency) {
        return new TransactionDto(tx.id(), tx.amount().toDecimal(), tx.currency(),
                amountInTargetCurrency, tx.iban(), tx.valueDate(), tx.type(), tx.description());
    }
}
