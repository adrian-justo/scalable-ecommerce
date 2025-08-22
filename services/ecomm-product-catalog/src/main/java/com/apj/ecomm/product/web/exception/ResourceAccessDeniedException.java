package com.apj.ecomm.product.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = 8084331469502201985L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("product");
	}

}