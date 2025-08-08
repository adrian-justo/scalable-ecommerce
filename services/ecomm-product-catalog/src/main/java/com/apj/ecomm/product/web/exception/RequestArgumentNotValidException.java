package com.apj.ecomm.product.web.exception;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestArgumentNotValidException extends IllegalArgumentException {

	private static final long serialVersionUID = -8519607397465066829L;

	private final Map<String, List<String>> errors;

}