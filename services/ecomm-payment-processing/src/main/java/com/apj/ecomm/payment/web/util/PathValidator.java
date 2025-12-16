package com.apj.ecomm.payment.web.util;

import com.apj.ecomm.payment.web.exception.ResourceNotFoundException;

public final class PathValidator {

	private PathValidator() {
		// Prevent instantiation
	}

	public static void paymentId(final long variable) {
		if (variable <= 0)
			throw new ResourceNotFoundException();
	}

}
