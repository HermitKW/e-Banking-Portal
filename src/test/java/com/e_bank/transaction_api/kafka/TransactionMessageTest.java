package com.e_bank.transaction_api.kafka;

import com.e_bank.transaction_api.domain.Transaction;
import com.e_bank.transaction_api.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionMessageTest {

    @Test
    void mapsToDomain() {
        var msg = new TransactionMessage("t1", -7500, "CHF", "CH93", "2020-10-01", "Online payment");
        Transaction tx = msg.toDomain();
        assertEquals("t1", tx.id());
        assertEquals(-7500, tx.amountMinorUnits());
        assertEquals("CHF", tx.currency());
        assertEquals("CH93", tx.iban());
        assertEquals(LocalDate.of(2020, 10, 1), tx.valueDate());
        assertEquals(TransactionType.DEBIT, tx.type());
    }

    @Test
    void nullDescriptionBecomesEmpty() {
        var msg = new TransactionMessage("t1", 100, "GBP", "GB00", "2020-10-01", null);
        assertEquals("", msg.toDomain().description());
    }
}
