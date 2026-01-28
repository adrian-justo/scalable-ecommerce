package com.apj.ecomm.notification;

import org.springframework.boot.SpringApplication;

public class TestEcommNotificationApplication {

	public static void main(final String[] args) {
		SpringApplication.from(EcommNotificationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
