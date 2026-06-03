package com.e_bank.transaction_api.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

/** Per-page credit/debit totals in the requested target currency. */
public record PageSummary(
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalCredit,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalDebit,
        String currency
) {
}
