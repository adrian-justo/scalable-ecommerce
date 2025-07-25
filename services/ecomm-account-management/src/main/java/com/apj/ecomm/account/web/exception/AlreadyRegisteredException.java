package com.apj.ecomm.account.web.exception;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlreadyRegisteredException extends RuntimeException {

	private static final long serialVersionUID = -1569155018141774214L;

	private final Map<String, List<String>> errors;

	public Map<String, List<String>> getErrors() {
		errors.forEach((key, value) -> errors.put(key, List.of(value + " has already been registered")));
		return errors;
	}

}