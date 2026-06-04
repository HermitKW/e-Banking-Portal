package com.e_bank.transaction_api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Resource-server JWT settings: where to fetch keys, and the expected issuer/audience. */
@ConfigurationProperties("app.security")
public record SecurityProperties(String jwkSetUri, String issuer, String audience) {
}
