package com.apj.ecomm.cart.domain.model;

import java.math.BigDecimal;

import com.apj.ecomm.cart.web.client.product.ProductCatalog;

public record CartItemCatalog(ProductCatalog product, Integer quantity) {

	public BigDecimal getTotalPrice() {
		return BigDecimal.valueOf(quantity).multiply(product.price());
	}

}
