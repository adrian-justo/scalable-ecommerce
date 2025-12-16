package com.apj.ecomm.order.web.messaging.payment;

import com.apj.ecomm.order.domain.Status;

public record UpdateOrderStatusEvent(String buyerId, Status status, String paymentIntentId) {

	public UpdateOrderStatusEvent(final String buyerId, final Status status) {
		this(buyerId, status, null);
	}

}