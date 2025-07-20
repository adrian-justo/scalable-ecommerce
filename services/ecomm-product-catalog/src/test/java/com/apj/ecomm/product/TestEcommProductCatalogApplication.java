package com.apj.ecomm.product;

import org.springframework.boot.SpringApplication;

public class TestEcommProductCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.from(EcommProductCatalogApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
