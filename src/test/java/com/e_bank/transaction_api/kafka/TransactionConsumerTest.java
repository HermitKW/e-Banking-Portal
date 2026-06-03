package com.e_bank.transaction_api.kafka;

import com.e_bank.transaction_api.store.InMemoryTransactionStore;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionConsumerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void ingestsValidJsonIntoStore() {
        var store = new InMemoryTransactionStore();
        var consumer = new TransactionConsumer(store, objectMapper);

        consumer.ingest("{\"id\":\"t1\",\"amountMinorUnits\":10000,\"currency\":\"GBP\","
                + "\"iban\":\"GB00\",\"valueDate\":\"2020-10-01\",\"description\":\"x\"}");

        List<?> result = store.findByIbansAndMonth(Set.of("GB00"), YearMonth.of(2020, 10));
        assertEquals(1, result.size());
    }

    @Test
    void skipsMalformedJsonWithoutThrowing() {
        var store = new InMemoryTransactionStore();
        var consumer = new TransactionConsumer(store, objectMapper);

        assertDoesNotThrow(() -> consumer.ingest("not json"));
        assertTrue(store.findByIbansAndMonth(Set.of("GB00"), YearMonth.of(2020, 10)).isEmpty());
    }
}
