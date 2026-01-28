package com.apj.ecomm.order.web.messaging.account;

import java.util.Set;

public record UserResponse(String name, String shopName, String address, String email, String mobileNo,
		Set<NotificationType> notificationTypes) {
}