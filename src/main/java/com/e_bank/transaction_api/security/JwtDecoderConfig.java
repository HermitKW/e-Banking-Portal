package com.e_bank.transaction_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.ArrayList;
import java.util.List;

/** Builds a JWT decoder that validates signature (via JWKS), expiry, issuer and audience. */
@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(SecurityProperties properties) {
        // Lazy decoder: keys are fetched on first use, so the context starts without reaching the IdP.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri()).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        if (properties.issuer() != null && !properties.issuer().isBlank()) {
            validators.add(new JwtIssuerValidator(properties.issuer()));
        }
        if (properties.audience() != null && !properties.audience().isBlank()) {
            validators.add(new AudienceValidator(properties.audience()));
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
