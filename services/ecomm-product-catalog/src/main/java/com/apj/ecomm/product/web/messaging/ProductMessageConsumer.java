package com.apj.ecomm.product.web.messaging;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.product.domain.IProductService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ProductMessageConsumer {

	private final IProductService service;

	@Bean
	Consumer<ShopNameUpdatedEvent> syncShopName() {
		// Two phase query used to ensure write-through cache consistency
		return data -> service.getProductsBy(data.shopId()).forEach(id -> service.update(data.shopName(), id));
	}

}
