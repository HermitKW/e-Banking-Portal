package com.e_bank.transaction_api.domain;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;

/**
 * Read-model port for the transactions query. Swappable implementation:
 * in-memory for this POC; Kafka Streams + RocksDB in production.
 */
public interface TransactionStore {

    /** Transactions for any of the given IBANs within the month, ordered by value date then id. */
    List<Transaction> findByIbansAndMonth(Collection<String> ibans, YearMonth yearMonth);

    /** Ingest a transaction (called by the Kafka consumer). */
    void add(Transaction transaction);
}
