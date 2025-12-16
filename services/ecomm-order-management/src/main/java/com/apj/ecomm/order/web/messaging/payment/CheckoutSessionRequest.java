package com.apj.ecomm.order.web.messaging.payment;

import java.util.List;

import com.apj.ecomm.order.domain.model.OrderResponse;

public record CheckoutSessionRequest(List<OrderResponse> orders) {
}