package com.apj.ecomm.account.web.client.cart;

import java.math.BigDecimal;

import com.apj.ecomm.account.web.client.product.ProductCatalog;

public record CartItemCatalog(ProductCatalog product, Integer quantity) {

	public BigDecimal getTotalPrice() {
		return BigDecimal.valueOf(quantity).multiply(product.price());
	}

}
