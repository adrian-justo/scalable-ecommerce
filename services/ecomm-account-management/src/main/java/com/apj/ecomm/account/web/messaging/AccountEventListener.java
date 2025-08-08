package com.apj.ecomm.account.web.messaging;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.apj.ecomm.account.domain.model.ShopNameUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AccountEventListener {

	private final StreamBridge streamBridge;

	@TransactionalEventListener
	void syncShopName(final ShopNameUpdatedEvent data) {
		streamBridge.send("syncShopName-out-0", data);
	}

}
