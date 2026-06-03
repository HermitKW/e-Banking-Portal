package com.e_bank.transaction_api.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Read-side projection of one customer's transactions for a single calendar month.
 *
 * <p>This is a <em>query-time</em> view, not a stored aggregate: it is assembled on demand from
 * the transactions of the IBANs the authenticated customer owns. It deliberately holds only
 * source-currency facts (the transactions themselves) — pagination and per-page credit/debit
 * totals in a requested target currency are computed by the API layer, so the same read model
 * serves any target currency and any rate without re-materialisation.</p>
 */
public record MonthlyStatement(
        String customerId,
        YearMonth yearMonth,
        List<Transaction> transactions
) {
    public MonthlyStatement {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        transactions = List.copyOf(transactions); // defensive, immutable copy
    }
}
