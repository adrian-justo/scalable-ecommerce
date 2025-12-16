package com.apj.ecomm.payment.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 2665959191009617206L;

	private final String resource;

	public ResourceNotFoundException() {
		this("Payment");
	}

}