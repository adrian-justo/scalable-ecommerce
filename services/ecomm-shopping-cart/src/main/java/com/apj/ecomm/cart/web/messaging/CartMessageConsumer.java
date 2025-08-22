package com.apj.ecomm.cart.web.messaging;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.cart.domain.ICartService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CartMessageConsumer {

	private final ICartService service;

	@Bean
	Consumer<CreateCartEvent> createIfNotExist() {
		return data -> service.createCart(data.buyerId());
	}

}
