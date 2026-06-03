package com.e_bank.transaction_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** Integration smoke test: boots the full context against a Kafka Testcontainer. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TransactionApiApplicationIT {

	@Test
	void contextLoads() {
	}

}
