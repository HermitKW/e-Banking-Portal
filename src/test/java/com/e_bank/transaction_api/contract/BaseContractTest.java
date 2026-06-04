package com.e_bank.transaction_api.contract;

import com.e_bank.transaction_api.api.TransactionController;
import com.e_bank.transaction_api.api.dto.PageSummary;
import com.e_bank.transaction_api.api.dto.TransactionDto;
import com.e_bank.transaction_api.api.dto.TransactionPageResponse;
import com.e_bank.transaction_api.domain.TransactionType;
import com.e_bank.transaction_api.query.TransactionQueryService;
import com.e_bank.transaction_api.security.SecurityConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/** Base for generated Spring Cloud Contract tests: stubs the query service and a JWT, through the real chain. */
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
public abstract class BaseContractTest {

    @Autowired
    WebApplicationContext context;

    @MockitoBean
    TransactionQueryService queryService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        TransactionPageResponse sample = new TransactionPageResponse(
                YearMonth.of(2020, 10), "CHF", Instant.parse("2026-06-03T09:00:00Z"),
                0, 50, 1, 1,
                new PageSummary(new BigDecimal("110.00"), new BigDecimal("-55.00"), "CHF"),
                List.of(new TransactionDto("t1", new BigDecimal("100.00"), "GBP", new BigDecimal("110.00"),
                        "CH93-0000-0000-0000-0000-0", LocalDate.parse("2020-10-01"),
                        TransactionType.CREDIT, "salary")));
        when(queryService.getTransactions(any(), any(), any(), anyInt(), anyInt())).thenReturn(sample);

        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").subject("P-0123456789").build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        RestAssuredMockMvc.webAppContextSetup(context, springSecurity());
    }
}
