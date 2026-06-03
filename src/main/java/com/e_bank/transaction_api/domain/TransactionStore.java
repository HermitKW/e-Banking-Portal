package com.e_bank.transaction_api.domain;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;

/**
 * Port (hexagonal-style) for the read model that backs the transactions query.
 *
 * <p>The query pattern is "all transactions for a set of owned IBANs within a calendar month",
 * so that is exactly what this interface exposes. Keeping it an interface lets the data-access
 * implementation be swapped without touching the API layer:</p>
 * <ul>
 *   <li><b>This assessment (POC):</b> an in-process, IBAN-keyed implementation populated by a
 *       Kafka consumer (see {@code InMemoryTransactionStore}, added in Stage 2).</li>
 *   <li><b>Production target:</b> a Kafka Streams + RocksDB state store materialising the same
 *       view durably and partitioned — documented in the README.</li>
 * </ul>
 */
public interface TransactionStore {

    /**
     * Returns the transactions belonging to any of the given IBANs whose value date falls within
     * the supplied calendar month, ordered by value date then id.
     *
     * @param ibans     the IBANs owned by the authenticated customer (may be empty)
     * @param yearMonth the calendar month to query
     * @return an immutable, ordered list (never {@code null}; empty if there are no matches)
     */
    List<Transaction> findByIbansAndMonth(Collection<String> ibans, YearMonth yearMonth);

    /** Ingests a transaction into the read model (called by the Kafka consumer). */
    void add(Transaction transaction);
}
