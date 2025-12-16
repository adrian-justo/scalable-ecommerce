package com.apj.ecomm.payment.web.messaging.order;

public record UpdateOrderStatusEvent(String buyerId, Status status, String paymentIntentId) {

	public UpdateOrderStatusEvent(final String buyerId, final Status status) {
		this(buyerId, status, null);
	}

}