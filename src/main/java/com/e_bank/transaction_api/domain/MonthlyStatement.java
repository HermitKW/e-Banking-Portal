package com.e_bank.transaction_api.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Query-time view of one customer's transactions for a calendar month.
 * Holds source-currency facts only; pagination and FX totals are applied by the API layer.
 */
public record MonthlyStatement(
        String customerId,
        YearMonth yearMonth,
        List<Transaction> transactions
) {
    public MonthlyStatement {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        transactions = List.copyOf(transactions);
    }
}
