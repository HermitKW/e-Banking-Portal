package com.e_bank.transaction_api.api;

import com.e_bank.transaction_api.api.dto.TransactionPageResponse;
import com.e_bank.transaction_api.query.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/** Returns the authenticated customer's monthly transactions with per-page FX totals. */
@RestController
@RequestMapping("/api/v1/transactions")
@Validated
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionQueryService queryService;

    public TransactionController(TransactionQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "List the authenticated customer's transactions for a calendar month")
    public TransactionPageResponse getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @RequestParam String targetCurrency,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        // Customer identity comes only from the validated token, never a request parameter.
        return queryService.getTransactions(jwt.getSubject(), yearMonth, targetCurrency, page, size);
    }
}
