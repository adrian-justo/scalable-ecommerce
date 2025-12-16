package com.apj.ecomm.payment.web.messaging.order;

import java.util.List;

public record OrderResponse(String buyerId, List<OrderItemResponse> products) {
}