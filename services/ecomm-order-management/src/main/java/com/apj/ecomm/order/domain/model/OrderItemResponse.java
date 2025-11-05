package com.apj.ecomm.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderItemResponse(Long productId, OrderItemDetailResponse productDetail, Integer quantity,
		BigDecimal totalPrice, Instant createdAt, Instant updatedAt) {
}