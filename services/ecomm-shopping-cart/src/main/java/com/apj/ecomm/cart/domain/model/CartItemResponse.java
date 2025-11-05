package com.apj.ecomm.cart.domain.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartItemResponse(Long id, Long productId, String shopId, Integer quantity, Instant createdAt,
		Instant updatedAt) {
}