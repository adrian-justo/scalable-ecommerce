package com.apj.ecomm.notification.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 80662375787347401L;

	private final String resource;

	public ResourceNotFoundException() {
		this("Notification");
	}

}