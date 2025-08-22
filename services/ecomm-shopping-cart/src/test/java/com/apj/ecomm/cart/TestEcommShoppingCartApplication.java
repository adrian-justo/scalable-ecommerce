package com.apj.ecomm.cart;

import org.springframework.boot.SpringApplication;

public class TestEcommShoppingCartApplication {

	public static void main(final String[] args) {
		SpringApplication.from(EcommShoppingCartApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
