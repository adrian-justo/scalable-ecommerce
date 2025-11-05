package com.apj.ecomm.order.domain;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
class OrderItemId implements Serializable {

	private static final long serialVersionUID = 5661037351750884089L;

	private Long id;

	private Long productId;

	@Override
	public boolean equals(final Object o) {
		if (o instanceof final OrderItemId item)
			return Objects.equals(id, item.getId()) && Objects.equals(productId, item.getProductId());
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, productId);
	}

}