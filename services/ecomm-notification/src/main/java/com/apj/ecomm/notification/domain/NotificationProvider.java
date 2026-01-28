package com.apj.ecomm.notification.domain;

import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;

interface NotificationProvider {

	static String getBy(final NotificationType type) {
		return switch (type) {
			case EMAIL -> EmailNotificationProvider.CURRENT;
			case SMS -> SMSNotificationProvider.CURRENT;
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}

	Message generate(NotificationRequest request, NotificationMapper mapper);

	String send(Message message);

}
