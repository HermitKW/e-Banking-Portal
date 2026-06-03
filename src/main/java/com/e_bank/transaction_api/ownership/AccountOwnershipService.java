package com.e_bank.transaction_api.ownership;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves which IBANs a customer owns (query-time ownership; stub backed by config). */
@Service
public class AccountOwnershipService {

    private final Map<String, Set<String>> ownedIbansByCustomer;

    public AccountOwnershipService(AccountOwnershipProperties properties) {
        Map<String, List<String>> accounts = properties.accounts() == null ? Map.of() : properties.accounts();
        this.ownedIbansByCustomer = accounts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Set.copyOf(e.getValue())));
    }

    /** IBANs owned by the customer; empty if the customer is unknown. */
    public Set<String> findOwnedIbans(String customerId) {
        return ownedIbansByCustomer.getOrDefault(customerId, Set.of());
    }
}
