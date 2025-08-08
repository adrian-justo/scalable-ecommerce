package com.apj.ecomm.account;

import org.springframework.boot.SpringApplication;

public class TestEcommAccountManagementApplication {

	public static void main(final String[] args) {
		SpringApplication.from(EcommAccountManagementApplication::main)
			.with(TestcontainersConfiguration.class)
			.run(args);
	}

}
