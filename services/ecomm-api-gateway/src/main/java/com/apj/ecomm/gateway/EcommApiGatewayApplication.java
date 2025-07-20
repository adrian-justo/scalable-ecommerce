package com.apj.ecomm.gateway;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class EcommApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommApiGatewayApplication.class, args);
	}

	@Bean
	KeyResolver ipKeyResolver() {
		return exchange -> Mono
				.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
	}

}
