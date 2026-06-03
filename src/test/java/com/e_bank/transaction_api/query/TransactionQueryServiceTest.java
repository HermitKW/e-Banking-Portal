package com.e_bank.transaction_api.query;

import com.e_bank.transaction_api.api.dto.TransactionDto;
import com.e_bank.transaction_api.api.dto.TransactionPageResponse;
import com.e_bank.transaction_api.domain.Transaction;
import com.e_bank.transaction_api.fx.ExchangeRateProvider;
import com.e_bank.transaction_api.ownership.AccountOwnershipProperties;
import com.e_bank.transaction_api.ownership.AccountOwnershipService;
import com.e_bank.transaction_api.store.InMemoryTransactionStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionQueryServiceTest {

    private static final YearMonth OCT = YearMonth.of(2020, 10);

    private TransactionQueryService service(InMemoryTransactionStore store, ExchangeRateProvider fx) {
        var ownership = new AccountOwnershipService(
                new AccountOwnershipProperties(Map.of("P-001", List.of("IBAN1", "IBAN2"))));
        var clock = Clock.fixed(Instant.parse("2026-06-03T09:00:00Z"), ZoneOffset.UTC);
        return new TransactionQueryService(store, ownership, fx, clock);
    }

    private static Transaction tx(String id, long minor, String iban, String date) {
        return new Transaction(id, minor, "GBP", iban, LocalDate.parse(date), "d");
    }

    @Test
    void convertsTotalsAndExcludesUnownedIbans() {
        var store = new InMemoryTransactionStore();
        store.add(tx("a", 10_000, "IBAN1", "2020-10-01"));   // +100.00 GBP credit
        store.add(tx("b", -5_000, "IBAN2", "2020-10-02"));   // -50.00 GBP debit
        store.add(tx("c", 2_000, "IBAN3", "2020-10-03"));    // not owned -> excluded
        ExchangeRateProvider fx = (from, to) -> new BigDecimal("1.10");

        TransactionPageResponse resp = service(store, fx).getTransactions("P-001", OCT, "CHF", 0, 50);

        assertEquals(2, resp.totalElements());
        assertEquals(1, resp.totalPages());
        assertEquals("CHF", resp.targetCurrency());
        assertEquals(Instant.parse("2026-06-03T09:00:00Z"), resp.fxRateAsOf());
        assertEquals(new BigDecimal("110.00"), resp.pageTotals().totalCredit());
        assertEquals(new BigDecimal("-55.00"), resp.pageTotals().totalDebit());
        assertEquals(List.of("a", "b"), resp.transactions().stream().map(TransactionDto::id).toList());
    }

    @Test
    void paginatesToRequestedPage() {
        var store = new InMemoryTransactionStore();
        for (int i = 0; i < 5; i++) {
            store.add(tx("t" + i, 1_000, "IBAN1", "2020-10-0" + (i + 1)));
        }
        ExchangeRateProvider fx = (from, to) -> BigDecimal.ONE;

        TransactionPageResponse resp = service(store, fx).getTransactions("P-001", OCT, "GBP", 1, 2);

        assertEquals(5, resp.totalElements());
        assertEquals(3, resp.totalPages());
        assertEquals(List.of("t2", "t3"), resp.transactions().stream().map(TransactionDto::id).toList());
    }

    @Test
    void failsClosedWhenFxUnavailable() {
        var store = new InMemoryTransactionStore();
        store.add(tx("a", 10_000, "IBAN1", "2020-10-01"));
        ExchangeRateProvider fx = (from, to) -> {
            throw new RuntimeException("fx down");
        };
        var svc = service(store, fx);
        assertThrows(RuntimeException.class, () -> svc.getTransactions("P-001", OCT, "CHF", 0, 50));
    }
}
