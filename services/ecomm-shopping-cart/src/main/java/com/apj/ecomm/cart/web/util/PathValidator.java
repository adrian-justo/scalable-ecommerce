package com.apj.ecomm.cart.web.util;

import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;

public final class PathValidator {

	private PathValidator() {
		// Prevent instantiation
	}

	public static void cartId(final long variable) {
		if (variable <= 0)
			throw new ResourceNotFoundException();
	}

	public static void productId(final long variable) {
		if (variable <= 0)
			throw new ResourceNotFoundException("Product");
	}

}
