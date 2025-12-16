package com.apj.ecomm.payment.web.messaging.order;

import java.math.BigDecimal;

public record OrderItemDetailResponse(String image, String name, BigDecimal price) {

	public BigDecimal price() {
		return price.multiply(BigDecimal.valueOf(100));
	}

}