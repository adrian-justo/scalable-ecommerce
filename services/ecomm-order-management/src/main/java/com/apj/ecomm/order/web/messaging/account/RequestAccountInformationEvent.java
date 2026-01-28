package com.apj.ecomm.order.web.messaging.account;

import java.util.Set;

public record RequestAccountInformationEvent(String buyerId, Set<String> userIds) {

	public RequestAccountInformationEvent {
		userIds.add(buyerId);
	}

}