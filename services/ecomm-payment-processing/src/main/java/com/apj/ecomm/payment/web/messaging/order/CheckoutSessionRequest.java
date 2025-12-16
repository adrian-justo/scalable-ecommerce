package com.apj.ecomm.payment.web.messaging.order;

import java.util.List;

public record CheckoutSessionRequest(List<OrderResponse> orders) {
}