package com.apj.ecomm.cart.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -2887791564832251506L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("cart");
	}

}