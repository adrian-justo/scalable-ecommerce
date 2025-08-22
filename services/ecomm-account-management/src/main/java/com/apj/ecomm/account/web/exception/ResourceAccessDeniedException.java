package com.apj.ecomm.account.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = 4459921620421386114L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("user");
	}

}