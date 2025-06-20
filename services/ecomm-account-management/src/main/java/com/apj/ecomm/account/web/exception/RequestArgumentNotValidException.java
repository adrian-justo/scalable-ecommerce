package com.apj.ecomm.account.web.exception;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestArgumentNotValidException extends IllegalArgumentException {
	private final Map<String, List<String>> errors;
}