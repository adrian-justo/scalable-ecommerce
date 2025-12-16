package com.apj.ecomm.account.web.messaging;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.apj.ecomm.account.web.messaging.cart.CreateCartEvent;
import com.apj.ecomm.account.web.messaging.product.ShopNameUpdatedEvent;
import com.apj.ecomm.account.web.messaging.product.ShopStatusUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AccountEventListener {

	private final StreamBridge streamBridge;

	@TransactionalEventListener
	void syncShopName(final ShopNameUpdatedEvent data) {
		streamBridge.send("syncShopName-out-0", data);
	}

	@TransactionalEventListener
	void createIfNotExist(final CreateCartEvent data) {
		streamBridge.send("createIfNotExist-out-0", data);
	}

	@TransactionalEventListener
	void syncShopStatus(final ShopStatusUpdatedEvent data) {
		streamBridge.send("syncShopStatus-out-0", data);
	}

}
