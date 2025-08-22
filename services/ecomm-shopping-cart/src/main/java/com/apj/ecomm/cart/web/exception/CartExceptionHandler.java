package com.apj.ecomm.cart.web.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.cart.constants.AppConstants;

@RestControllerAdvice
class CartExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handle(final ResourceNotFoundException e) {
		return getDetail(HttpStatus.NOT_FOUND, e.getResource() + AppConstants.MSG_NOT_FOUND);
	}

	@ExceptionHandler(ResourceAccessDeniedException.class)
	ProblemDetail handle(final ResourceAccessDeniedException e) {
		return getDetail(HttpStatus.NOT_FOUND, AppConstants.MSG_ACCESS_DENIED + e.getResource());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ProblemDetail handle(final MissingRequestHeaderException e) {
		return getDetail(HttpStatus.BAD_REQUEST,
				"Required header is missing. Please ensure you are logged in and are using the correct url.");
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	ProblemDetail handle(final HandlerMethodValidationException e) {
		return getDetail(HttpStatus.BAD_REQUEST, AppConstants.MSG_BAD_REQUEST,
				e.getParameterValidationResults()
					.stream()
					.map(ParameterValidationResult::getResolvableErrors)
					.flatMap(List::stream)
					.map(FieldError.class::cast)
					.collect(Collectors.groupingBy(FieldError::getField,
							Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class })
	ProblemDetail handle(final Exception e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	private ProblemDetail getDetail(final HttpStatus status, final String title) {
		final var problemDetail = ProblemDetail.forStatus(status);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	private ProblemDetail getDetail(final HttpStatus status, final String title,
			final Map<String, List<String>> errors) {
		final var problemDetail = getDetail(status, title);
		problemDetail.setProperty("errors", errors);
		return problemDetail;
	}

}
