package com.e_bank.transaction_api.ownership;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountOwnershipServiceTest {

    @Test
    void returnsOwnedIbans() {
        var service = new AccountOwnershipService(
                new AccountOwnershipProperties(Map.of("P-001", List.of("IBAN1", "IBAN2"))));
        assertEquals(Set.of("IBAN1", "IBAN2"), service.findOwnedIbans("P-001"));
    }

    @Test
    void emptyForUnknownCustomer() {
        var service = new AccountOwnershipService(
                new AccountOwnershipProperties(Map.of("P-001", List.of("IBAN1"))));
        assertTrue(service.findOwnedIbans("P-999").isEmpty());
    }

    @Test
    void handlesNullAccounts() {
        var service = new AccountOwnershipService(new AccountOwnershipProperties(null));
        assertTrue(service.findOwnedIbans("P-001").isEmpty());
    }
}
