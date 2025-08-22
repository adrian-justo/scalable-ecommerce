package com.apj.ecomm.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcommShoppingCartApplication {

	public static void main(final String[] args) {
		SpringApplication.run(EcommShoppingCartApplication.class, args);
	}

}
