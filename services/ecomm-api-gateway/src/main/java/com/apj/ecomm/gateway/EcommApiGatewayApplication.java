package com.apj.ecomm.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.apj.ecomm.gateway.constants.Paths;

@SpringBootApplication
@EnableConfigurationProperties(Paths.class)
public class EcommApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommApiGatewayApplication.class, args);
	}

}
