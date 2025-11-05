package com.apj.ecomm.order.web.util;

import com.apj.ecomm.order.web.exception.ResourceNotFoundException;

public final class PathValidator {

	private PathValidator() {
		// Prevent instantiation
	}

	public static void orderId(final long variable) {
		if (variable <= 0)
			throw new ResourceNotFoundException();
	}

}
