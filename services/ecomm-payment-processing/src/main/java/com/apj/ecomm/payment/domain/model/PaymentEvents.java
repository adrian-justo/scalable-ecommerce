package com.apj.ecomm.payment.domain.model;

import com.apj.ecomm.payment.web.messaging.cart.UpdateCartOrderedEvent;
import com.apj.ecomm.payment.web.messaging.order.UpdateOrderStatusEvent;

public record PaymentEvents(UpdatePaymentStatus updatePaymentStatus, UpdateOrderStatusEvent updateOrderStatus,
		UpdateCartOrderedEvent updateCartOrdered) {

	public PaymentEvents(final UpdatePaymentStatus updatePaymentStatus,
			final UpdateOrderStatusEvent updateOrderStatus) {
		this(updatePaymentStatus, updateOrderStatus, null);
	}

	public PaymentEvents(final UpdateOrderStatusEvent updateOrderStatus,
			final UpdateCartOrderedEvent updateCartOrdered) {
		this(null, updateOrderStatus, updateCartOrdered);
	}

}