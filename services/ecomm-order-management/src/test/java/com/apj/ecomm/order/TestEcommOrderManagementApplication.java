package com.apj.ecomm.order;

import org.springframework.boot.SpringApplication;

public class TestEcommOrderManagementApplication {

	public static void main(String[] args) {
		SpringApplication.from(EcommOrderManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
