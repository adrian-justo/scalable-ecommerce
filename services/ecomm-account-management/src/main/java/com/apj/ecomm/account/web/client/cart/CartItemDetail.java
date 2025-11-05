package com.apj.ecomm.account.web.client.cart;

import java.math.BigDecimal;

import com.apj.ecomm.account.web.client.product.ProductResponse;

public record CartItemDetail(ProductResponse product, Integer quantity) {

	public BigDecimal getTotalPrice() {
		return BigDecimal.valueOf(quantity).multiply(product.price());
	}

}
