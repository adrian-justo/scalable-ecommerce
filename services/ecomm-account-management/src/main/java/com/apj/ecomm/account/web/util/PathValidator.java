package com.apj.ecomm.account.web.util;

import org.apache.commons.lang3.StringUtils;

import com.apj.ecomm.account.web.exception.ResourceNotFoundException;

public final class PathValidator {

	private PathValidator() {
		// Prevent instantiation
	}

	public static void username(final String variable) {
		if (StringUtils.isBlank(variable))
			throw new ResourceNotFoundException();
	}

	public static void productId(final long variable) {
		if (variable <= 0)
			throw new ResourceNotFoundException("Product");
	}

}
