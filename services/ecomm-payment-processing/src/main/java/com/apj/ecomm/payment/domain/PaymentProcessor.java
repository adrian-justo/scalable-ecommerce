package com.apj.ecomm.payment.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;

import com.apj.ecomm.payment.domain.model.PaymentEvents;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;

public interface PaymentProcessor {

	String getValue(SessionStatus status);

	Payment create(List<OrderResponse> orders);

	void expire(String id);

	Optional<PaymentEvents> getEvents(String payload, HttpHeaders headers);

}
