package com.apj.ecomm.account.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 7302863915107596158L;

	private final String resource;

	public ResourceNotFoundException() {
		this("User");
	}

}