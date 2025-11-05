package com.apj.ecomm.cart.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record CartDetailResponse(List<CartItemDetail> products) {

	public BigDecimal getTotal() {
		return products.stream().map(CartItemDetail::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public Integer getTotalProducts() {
		return products.size();
	}

	public Integer getTotalQuantity() {
		return products.stream().map(CartItemDetail::quantity).reduce(0, Integer::sum);
	}

}
