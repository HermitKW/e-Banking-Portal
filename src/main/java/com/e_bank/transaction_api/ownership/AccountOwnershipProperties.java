package com.e_bank.transaction_api.ownership;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/** Demo customer -> owned IBANs mapping (stub for the core Account Service). */
@ConfigurationProperties("app.ownership")
public record AccountOwnershipProperties(Map<String, List<String>> accounts) {
}
