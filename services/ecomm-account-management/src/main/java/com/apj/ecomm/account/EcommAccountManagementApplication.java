package com.apj.ecomm.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcommAccountManagementApplication {

	public static void main(final String[] args) {
		SpringApplication.run(EcommAccountManagementApplication.class, args);
	}

}
