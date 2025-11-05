package com.apj.ecomm.product.domain.model;

public record UpdateProductFromMessageRequest(String shopName, Integer quantity, boolean isOrder, Boolean active) {

	public UpdateProductFromMessageRequest(final String shopName) {
		this(shopName, null, false, null);
	}

	public UpdateProductFromMessageRequest(final Integer quantity, final boolean isOrder) {
		this(null, quantity, isOrder, null);
	}

	public UpdateProductFromMessageRequest(final Boolean active) {
		this(null, null, false, active);
	}

}