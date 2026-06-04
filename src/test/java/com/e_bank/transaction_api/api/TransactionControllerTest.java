package com.e_bank.transaction_api.api;

import com.e_bank.transaction_api.api.dto.PageSummary;
import com.e_bank.transaction_api.api.dto.TransactionPageResponse;
import com.e_bank.transaction_api.query.TransactionQueryService;
import com.e_bank.transaction_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionQueryService queryService;

    @MockitoBean
    JwtDecoder jwtDecoder; // satisfies the resource-server chain; real decoding is bypassed by jwt()

    @Test
    void returnsPageForAuthenticatedCustomer() throws Exception {
        var response = new TransactionPageResponse(YearMonth.of(2020, 10), "CHF",
                Instant.parse("2026-06-03T09:00:00Z"), 0, 50, 1, 1,
                new PageSummary(new BigDecimal("110.00"), new BigDecimal("0.00"), "CHF"), List.of());
        when(queryService.getTransactions(eq("P-0123456789"), eq(YearMonth.of(2020, 10)), eq("CHF"), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("yearMonth", "2020-10")
                        .param("targetCurrency", "CHF")
                        .with(jwt().jwt(builder -> builder.subject("P-0123456789"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("CHF"))
                .andExpect(jsonPath("$.pageTotals.totalCredit").value("110.00"));
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .param("yearMonth", "2020-10")
                        .param("targetCurrency", "CHF"))
                .andExpect(status().isUnauthorized());
    }
}
