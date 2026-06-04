package com.e_bank.transaction_api.query;

import com.e_bank.transaction_api.api.dto.PageSummary;
import com.e_bank.transaction_api.api.dto.TransactionDto;
import com.e_bank.transaction_api.api.dto.TransactionPageResponse;
import com.e_bank.transaction_api.domain.Transaction;
import com.e_bank.transaction_api.domain.TransactionStore;
import com.e_bank.transaction_api.domain.TransactionType;
import com.e_bank.transaction_api.fx.CurrencyConverter;
import com.e_bank.transaction_api.fx.ExchangeRateProvider;
import com.e_bank.transaction_api.ownership.AccountOwnershipService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;

/** Builds a paginated, FX-converted view of a customer's transactions for a calendar month. */
@Service
public class TransactionQueryService {

    private final TransactionStore store;
    private final AccountOwnershipService ownershipService;
    private final ExchangeRateProvider exchangeRateProvider;
    private final Clock clock;

    public TransactionQueryService(TransactionStore store, AccountOwnershipService ownershipService,
                                   ExchangeRateProvider exchangeRateProvider, Clock clock) {
        this.store = store;
        this.ownershipService = ownershipService;
        this.exchangeRateProvider = exchangeRateProvider;
        this.clock = clock;
    }

    public TransactionPageResponse getTransactions(String customerId, YearMonth yearMonth,
                                                   String targetCurrency, int page, int size) {
        Currency target = Currency.getInstance(targetCurrency); // throws IllegalArgumentException -> 400
        Set<String> ownedIbans = ownershipService.findOwnedIbans(customerId);
        List<Transaction> all = store.findByIbansAndMonth(ownedIbans, yearMonth);

        int totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Transaction> pageItems = all.subList(fromIndex, toIndex);

        int scale = target.getDefaultFractionDigits();
        BigDecimal totalCredit = BigDecimal.ZERO.setScale(scale);
        BigDecimal totalDebit = BigDecimal.ZERO.setScale(scale);
        List<TransactionDto> dtos = new ArrayList<>(pageItems.size());
        for (Transaction tx : pageItems) {
            // getRate() is Caffeine-cached, so repeated calls for the same currency pair are
            // in-memory lookups (no network I/O). Pre-fetching distinct currencies would trim
            // even that overhead for large pages, but is unnecessary at this scale.
            BigDecimal rate = exchangeRateProvider.getRate(tx.currency(), targetCurrency);
            BigDecimal converted = CurrencyConverter.convert(tx.amount().toDecimal(), rate, target);
            dtos.add(TransactionDto.from(tx, converted));
            if (tx.type() == TransactionType.CREDIT) {
                totalCredit = totalCredit.add(converted);
            } else {
                totalDebit = totalDebit.add(converted);
            }
        }

        PageSummary pageTotals = new PageSummary(totalCredit, totalDebit, targetCurrency);
        return new TransactionPageResponse(yearMonth, targetCurrency, Instant.now(clock),
                page, size, totalElements, totalPages, pageTotals, dtos);
    }
}
