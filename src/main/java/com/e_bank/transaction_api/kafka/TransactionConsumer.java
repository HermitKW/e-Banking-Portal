package com.e_bank.transaction_api.kafka;

import com.e_bank.transaction_api.domain.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.format.DateTimeParseException;

/** Consumes the transactions topic (JSON) and populates the read model. */
@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final TransactionStore store;
    private final ObjectMapper objectMapper;

    public TransactionConsumer(TransactionStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.transactions-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String payload) {
        ingest(payload);
    }

    /** Parse one JSON payload and add it to the store; malformed payloads are skipped. */
    void ingest(String payload) {
        try {
            store.add(objectMapper.readValue(payload, TransactionMessage.class).toDomain());
        } catch (JacksonException | DateTimeParseException e) {
            log.warn("Skipping malformed transaction payload: {}", e.getMessage());
        }
    }
}
