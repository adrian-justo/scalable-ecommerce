package com.apj.ecomm.notification.web.util;

import org.apache.commons.lang3.StringUtils;

import com.apj.ecomm.notification.web.exception.ResourceNotFoundException;

public final class PathValidator {

	private PathValidator() {
		// Prevent instantiation
	}

	public static void notificationId(final String variable) {
		if (StringUtils.isBlank(variable))
			throw new ResourceNotFoundException();
	}

}
