package com.apj.ecomm.product.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 7573661197363198957L;

	private final String resource;

	public ResourceNotFoundException() {
		this("Product");
	}

}