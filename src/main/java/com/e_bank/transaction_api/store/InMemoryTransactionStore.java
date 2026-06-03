package com.e_bank.transaction_api.store;

import com.e_bank.transaction_api.domain.Transaction;
import com.e_bank.transaction_api.domain.TransactionStore;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, IBAN-keyed read model (POC).
 * Production target: a Kafka Streams + RocksDB state store behind the same port.
 */
@Component
public class InMemoryTransactionStore implements TransactionStore {

    // IBAN -> its transactions. Concurrent map; per-IBAN lists are snapshotted under lock for reads.
    private final Map<String, List<Transaction>> byIban = new ConcurrentHashMap<>();

    @Override
    public void add(Transaction transaction) {
        byIban.computeIfAbsent(transaction.iban(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(transaction);
    }

    @Override
    public List<Transaction> findByIbansAndMonth(Collection<String> ibans, YearMonth yearMonth) {
        return ibans.stream()
                .map(byIban::get)
                .filter(Objects::nonNull)
                .flatMap(list -> snapshot(list).stream())
                .filter(tx -> YearMonth.from(tx.valueDate()).equals(yearMonth))
                .sorted(Comparator.comparing(Transaction::valueDate).thenComparing(Transaction::id))
                .toList();
    }

    private static List<Transaction> snapshot(List<Transaction> list) {
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }
}
