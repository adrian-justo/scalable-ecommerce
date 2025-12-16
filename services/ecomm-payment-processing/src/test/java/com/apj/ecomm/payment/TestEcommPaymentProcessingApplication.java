package com.apj.ecomm.payment;

import org.springframework.boot.SpringApplication;

public class TestEcommPaymentProcessingApplication {

	public static void main(final String[] args) {
		SpringApplication.from(EcommPaymentProcessingApplication::main)
			.with(TestcontainersConfiguration.class)
			.run(args);
	}

}
