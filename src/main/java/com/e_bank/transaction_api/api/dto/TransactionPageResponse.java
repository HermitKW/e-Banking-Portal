package com.e_bank.transaction_api.api.dto;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

/** One page of monthly transactions plus per-page FX totals. */
public record TransactionPageResponse(
        YearMonth yearMonth,
        String targetCurrency,
        Instant fxRateAsOf,
        int page,
        int size,
        int totalElements,
        int totalPages,
        PageSummary pageTotals,
        List<TransactionDto> transactions
) {
}
