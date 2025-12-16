package com.apj.ecomm.payment.web.messaging.order;

public record OrderItemResponse(OrderItemDetailResponse productDetail, Integer quantity) {
}