package com.apj.ecomm.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcommOrderManagementApplication {

	public static void main(final String[] args) {
		SpringApplication.run(EcommOrderManagementApplication.class, args);
	}

}
