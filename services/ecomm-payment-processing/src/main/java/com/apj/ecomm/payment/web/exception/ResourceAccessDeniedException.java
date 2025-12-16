package com.apj.ecomm.payment.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -5385945117270711071L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("payment");
	}

}