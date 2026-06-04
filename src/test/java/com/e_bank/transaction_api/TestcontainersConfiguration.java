package com.e_bank.transaction_api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	KafkaContainer kafkaContainer() {
		// JVM-based Apache Kafka image, pinned. The kafka-native (GraalVM) image segfaults
		// on some CI container runtimes (e.g. CircleCI machine executors).
		return new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));
	}

}
