package com.e_bank.transaction_api.store;

import com.e_bank.transaction_api.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTransactionStoreTest {

    private static Transaction tx(String id, String iban, String date) {
        return new Transaction(id, 1000, "GBP", iban, LocalDate.parse(date), "d");
    }

    @Test
    void returnsOnlyMatchingIbansAndMonth() {
        var store = new InMemoryTransactionStore();
        store.add(tx("a", "IBAN1", "2020-10-05"));
        store.add(tx("b", "IBAN1", "2020-11-01")); // different month
        store.add(tx("c", "IBAN2", "2020-10-09"));
        store.add(tx("d", "IBAN3", "2020-10-09")); // not owned

        List<Transaction> result = store.findByIbansAndMonth(Set.of("IBAN1", "IBAN2"), YearMonth.of(2020, 10));

        assertEquals(List.of("a", "c"), result.stream().map(Transaction::id).toList());
    }

    @Test
    void ordersByValueDateThenId() {
        var store = new InMemoryTransactionStore();
        store.add(tx("z", "IBAN1", "2020-10-10"));
        store.add(tx("a", "IBAN1", "2020-10-10")); // same date -> id breaks the tie
        store.add(tx("m", "IBAN1", "2020-10-01"));

        List<Transaction> result = store.findByIbansAndMonth(Set.of("IBAN1"), YearMonth.of(2020, 10));

        assertEquals(List.of("m", "a", "z"), result.stream().map(Transaction::id).toList());
    }

    @Test
    void emptyForUnknownIbanOrNoIbans() {
        var store = new InMemoryTransactionStore();
        store.add(tx("a", "IBAN1", "2020-10-05"));
        assertTrue(store.findByIbansAndMonth(Set.of("UNKNOWN"), YearMonth.of(2020, 10)).isEmpty());
        assertTrue(store.findByIbansAndMonth(Set.of(), YearMonth.of(2020, 10)).isEmpty());
    }

    @Test
    void returnedListIsImmutable() {
        var store = new InMemoryTransactionStore();
        store.add(tx("a", "IBAN1", "2020-10-05"));
        List<Transaction> result = store.findByIbansAndMonth(Set.of("IBAN1"), YearMonth.of(2020, 10));
        assertThrows(UnsupportedOperationException.class, () -> result.add(tx("x", "IBAN1", "2020-10-06")));
    }
}
