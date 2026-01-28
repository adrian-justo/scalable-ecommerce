package com.apj.ecomm.notification.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = 484182127966979588L;

	private final String resource;

	public ResourceAccessDeniedException() {
		this("notification");
	}

}