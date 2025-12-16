package com.apj.ecomm.payment.web.messaging;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.payment.domain.IPaymentService;
import com.apj.ecomm.payment.web.messaging.order.CheckoutSessionRequest;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PaymentMessageConsumer {

	private final IPaymentService service;

	@Bean
	Consumer<CheckoutSessionRequest> createSession() {
		return data -> service.createSession(data.orders());
	}

}
