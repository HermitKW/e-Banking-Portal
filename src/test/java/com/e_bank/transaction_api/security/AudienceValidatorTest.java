package com.e_bank.transaction_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("transaction-api");

    private static Jwt jwt(List<String> audience) {
        Jwt.Builder builder = Jwt.withTokenValue("t").header("alg", "none").subject("P-0123456789");
        if (audience != null) {
            builder.audience(audience);
        }
        return builder.build();
    }

    @Test
    void succeedsWhenAudiencePresent() {
        assertFalse(validator.validate(jwt(List.of("transaction-api"))).hasErrors());
    }

    @Test
    void failsWhenAudienceIsDifferent() {
        assertTrue(validator.validate(jwt(List.of("other-service"))).hasErrors());
    }

    @Test
    void failsWhenAudienceMissing() {
        assertTrue(validator.validate(jwt(null)).hasErrors());
    }
}
