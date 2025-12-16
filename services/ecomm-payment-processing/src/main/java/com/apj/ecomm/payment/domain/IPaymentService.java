package com.apj.ecomm.payment.domain;

import java.util.List;
import java.util.Optional;

import com.apj.ecomm.payment.domain.model.PaymentResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;

public interface IPaymentService {

	String getSession(String buyerId);

	Optional<Payment> findOpenSession(String buyerId);

	PaymentResponse createSession(List<OrderResponse> orders);

	Payment update(Payment payment, SessionStatus status);

}
