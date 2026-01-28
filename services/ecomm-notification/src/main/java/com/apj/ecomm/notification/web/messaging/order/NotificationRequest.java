package com.apj.ecomm.notification.web.messaging.order;

import com.apj.ecomm.notification.domain.NotificationType;

public record NotificationRequest(Long orderId, String userId, Role role, String recipient, NotificationType type,
		Status status) {
}