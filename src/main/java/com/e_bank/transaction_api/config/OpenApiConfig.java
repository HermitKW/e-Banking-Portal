package com.e_bank.transaction_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI metadata and the bearer-JWT security scheme. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI transactionApiOpenApi() {
        SecurityScheme bearerJwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info().title("Transaction API").version("v1")
                        .description("Monthly account transactions with per-page FX totals."))
                .components(new Components().addSecuritySchemes("bearer-jwt", bearerJwt))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
