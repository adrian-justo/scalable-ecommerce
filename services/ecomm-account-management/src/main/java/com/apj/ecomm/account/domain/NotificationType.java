package com.apj.ecomm.account.domain;

import java.util.Arrays;

public enum NotificationType {

	EMAIL, SMS;

	public static boolean isValid(final NotificationType type) {
		return Arrays.stream(NotificationType.values()).anyMatch(type::equals);
	}

}
