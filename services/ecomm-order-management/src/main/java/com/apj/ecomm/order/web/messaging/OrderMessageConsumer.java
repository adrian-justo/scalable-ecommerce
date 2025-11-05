package com.apj.ecomm.order.web.messaging;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.order.domain.IOrderService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OrderMessageConsumer {

	private final IOrderService service;

	@Bean
	Function<AccountInformationDetails, ProductStockUpdate> updateInformationAndSendStockUpdate() {
		return data -> service.updateInformationAndGetStockUpdate(data.buyerId(), data.users());
	}

	@Bean
	Consumer<OrderedProductDetails> populateOrderItemDetail() {
		return data -> service.populateOrderItemDetail(data.buyerId(), data.details());
	}

}
