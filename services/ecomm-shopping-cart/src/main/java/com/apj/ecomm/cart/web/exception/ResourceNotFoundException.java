package com.apj.ecomm.cart.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -6479781460086926873L;

	private final String resource;

	public ResourceNotFoundException() {
		this("Cart");
	}

}