package com.apj.ecomm.payment.web.messaging;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.apj.ecomm.payment.web.messaging.cart.UpdateCartOrderedEvent;
import com.apj.ecomm.payment.web.messaging.order.UpdateOrderStatusEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PaymentEventListener {

	private final StreamBridge streamBridge;

	@TransactionalEventListener
	void updateOrderStatus(final UpdateOrderStatusEvent data) {
		streamBridge.send("updateOrderStatus-out-0", data);
	}

	@TransactionalEventListener
	void updateCartOrdered(final UpdateCartOrderedEvent data) {
		streamBridge.send("updateCartOrdered-out-0", data);
	}

}
