package com.apj.ecomm.product.web.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class ProductExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handle(ResourceNotFoundException e) {
		return getDetail(HttpStatus.NOT_FOUND, e.getResource() + " is not found");
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ProblemDetail handle(MissingRequestHeaderException e) {
		return getDetail(HttpStatus.BAD_REQUEST,
				"Required header is missing. Please ensure you are logged in and are using the correct url.");
	}

	@ExceptionHandler(RequestArgumentNotValidException.class)
	ProblemDetail handle(RequestArgumentNotValidException e) {
		return getDetail(HttpStatus.BAD_REQUEST, "Details provided is invalid", e.getErrors());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handle(MethodArgumentNotValidException e) {
		return getDetail(HttpStatus.BAD_REQUEST, "Details provided is invalid",
				e.getBindingResult().getFieldErrors().stream().collect(Collectors.groupingBy(FieldError::getField,
						Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	ProblemDetail handle(Exception e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	private ProblemDetail getDetail(HttpStatus status, String title) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(status);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	private ProblemDetail getDetail(HttpStatus status, String title, Map<String, List<String>> errors) {
		ProblemDetail problemDetail = getDetail(status, title);
		problemDetail.setProperty("errors", errors);
		return problemDetail;
	}

}
