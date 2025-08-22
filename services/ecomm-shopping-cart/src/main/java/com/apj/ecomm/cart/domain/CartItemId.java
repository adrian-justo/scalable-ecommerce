package com.apj.ecomm.cart.domain;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
class CartItemId implements Serializable {

	private static final long serialVersionUID = 5214237278149058025L;

	private Long id;

	private Long productId;

	@Override
	public boolean equals(final Object o) {
		if (o instanceof final CartItemId item)
			return Objects.equals(id, item.getId()) && Objects.equals(productId, item.getProductId());
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, productId);
	}

}
