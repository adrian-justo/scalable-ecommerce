package com.apj.ecomm.order.web.messaging;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.apj.ecomm.order.web.messaging.account.RequestAccountInformationEvent;
import com.apj.ecomm.order.web.messaging.cart.UpdateCartItemsEvent;
import com.apj.ecomm.order.web.messaging.product.ReturnProductStockEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrderEventListener {

	private final StreamBridge streamBridge;

	@TransactionalEventListener
	void requestAccountInformation(final RequestAccountInformationEvent data) {
		streamBridge.send("requestAccountInformation-out-0", data);
	}

	@TransactionalEventListener
	void updateCartItems(final UpdateCartItemsEvent data) {
		streamBridge.send("updateCartItems-out-0", data);
	}

	@TransactionalEventListener
	void returnProductStock(final ReturnProductStockEvent data) {
		streamBridge.send("returnProductStock-out-0", data);
	}

}
