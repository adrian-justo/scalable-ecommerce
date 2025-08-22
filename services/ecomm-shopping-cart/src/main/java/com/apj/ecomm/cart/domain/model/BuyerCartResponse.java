package com.apj.ecomm.cart.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record BuyerCartResponse(Long id, String buyerId, List<CartItemCatalog> products, boolean ordered) {

	public BigDecimal getTotal() {
		return products.stream().map(CartItemCatalog::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public Integer getTotalProducts() {
		return products.size();
	}

	public Integer getTotalQuantity() {
		return products.stream().map(CartItemCatalog::quantity).reduce(0, Integer::sum);
	}

}
