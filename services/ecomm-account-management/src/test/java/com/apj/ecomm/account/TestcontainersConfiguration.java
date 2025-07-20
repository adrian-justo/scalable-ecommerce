package com.apj.ecomm.account;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	static final int API_GATEWAY_PORT = 8080;

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
	}

	@Bean
	ServiceInstanceListSupplier eurekaContainer() {
		String serviceId = "ECOMM-API-GATEWAY";
		return ServiceInstanceListSuppliers.from(serviceId,
				new DefaultServiceInstance(serviceId + "-1", serviceId, "localhost", API_GATEWAY_PORT, false));
	}

}
