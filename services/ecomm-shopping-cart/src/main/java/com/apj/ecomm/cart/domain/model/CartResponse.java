package com.apj.ecomm.cart.domain.model;

import java.time.Instant;
import java.util.List;

public record CartResponse(Long id, String buyerId, List<CartItemResponse> products, boolean ordered, Instant createdAt,
		Instant updatedAt) {

}
