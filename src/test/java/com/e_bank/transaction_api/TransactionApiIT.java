package com.e_bank.transaction_api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end: produce JSON transactions to a real Kafka (Testcontainers), let the consumer
 * materialise them, stub the FX provider with WireMock, then assert the REST response and totals.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TransactionApiIT {

    private static final String OWNED_IBAN = "CH93-0000-0000-0000-0000-0"; // owned by P-0123456789
    private static final WireMockServer FX =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @BeforeAll
    static void startFx() {
        FX.start();
        // The stub only matches when the X-API-Key header is present, proving the client sends it.
        FX.stubFor(WireMock.get(WireMock.urlPathEqualTo("/rate"))
                .withQueryParam("from", WireMock.equalTo("GBP"))
                .withQueryParam("to", WireMock.equalTo("CHF"))
                .withHeader("X-API-Key", WireMock.equalTo("test-key"))
                .willReturn(WireMock.okJson("{\"from\":\"GBP\",\"to\":\"CHF\",\"rate\":1.10}")));
    }

    @AfterAll
    static void stopFx() {
        FX.stop();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.fx.base-url", FX::baseUrl);
        registry.add("app.fx.api-key", () -> "test-key");
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void producesConsumesAndReturnsConvertedTotals() {
        kafkaTemplate.send("transactions", "t1", message("t1", 10_000, OWNED_IBAN, "2020-10-01", "salary"));
        kafkaTemplate.send("transactions", "t2", message("t2", -5_000, OWNED_IBAN, "2020-10-02", "rent"));

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/transactions")
                                .param("yearMonth", "2020-10")
                                .param("targetCurrency", "CHF")
                                .with(jwt().jwt(builder -> builder.subject("P-0123456789"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.pageTotals.totalCredit").value("110.00"))
                        .andExpect(jsonPath("$.pageTotals.totalDebit").value("-55.00")));
    }

    private static String message(String id, long minor, String iban, String date, String description) {
        return ("{\"id\":\"%s\",\"amountMinorUnits\":%d,\"currency\":\"GBP\","
                + "\"iban\":\"%s\",\"valueDate\":\"%s\",\"description\":\"%s\"}")
                .formatted(id, minor, iban, date, description);
    }
}
