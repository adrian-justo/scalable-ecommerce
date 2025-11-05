package com.apj.ecomm.order.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -8554493343989092119L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("order");
	}

}