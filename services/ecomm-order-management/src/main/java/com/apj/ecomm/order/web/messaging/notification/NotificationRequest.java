package com.apj.ecomm.order.web.messaging.notification;

import com.apj.ecomm.order.domain.Status;
import com.apj.ecomm.order.web.messaging.account.NotificationType;

public record NotificationRequest(Long orderId, String userId, Role role, String recipient, NotificationType type,
		Status status) {
}