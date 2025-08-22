package com.apj.ecomm.cart.domain.model;

import java.math.BigDecimal;

import com.apj.ecomm.cart.web.client.product.ProductResponse;

public record CartItemDetail(ProductResponse product, Integer quantity) {

	public BigDecimal getTotalPrice() {
		return BigDecimal.valueOf(quantity).multiply(product.price());
	}

}
