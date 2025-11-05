package com.apj.ecomm.order.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2311039428409766357L;

	private final String resource;

	public ResourceNotFoundException() {
		this("Order");
	}

}