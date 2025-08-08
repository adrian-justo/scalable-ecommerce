package com.apj.ecomm.account.web.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.account.constants.AppConstants;

@RestControllerAdvice
class AccountExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handle(final ResourceNotFoundException e) {
		return getDetail(HttpStatus.NOT_FOUND, e.getResource() + AppConstants.MSG_NOT_FOUND);
	}

	@ExceptionHandler(AlreadyRegisteredException.class)
	ProblemDetail handle(final AlreadyRegisteredException e) {
		return getDetail(HttpStatus.CONFLICT, AppConstants.MSG_CONFLICT, e.getErrors());
	}

	@ExceptionHandler(BadCredentialsException.class)
	ProblemDetail handle(final BadCredentialsException e) {
		return getDetail(HttpStatus.UNAUTHORIZED, AppConstants.MSG_UNAUTHORIZED);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ProblemDetail handle(final MissingRequestHeaderException e) {
		return getDetail(HttpStatus.BAD_REQUEST,
				"Required header is missing. Please ensure you are logged in and are using the correct url.");
	}

	@ExceptionHandler(RequestArgumentNotValidException.class)
	ProblemDetail handle(final RequestArgumentNotValidException e) {
		return getDetail(HttpStatus.BAD_REQUEST, AppConstants.MSG_BAD_REQUEST, e.getErrors());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handle(final MethodArgumentNotValidException e) {
		return getDetail(HttpStatus.BAD_REQUEST, AppConstants.MSG_BAD_REQUEST,
				e.getBindingResult()
					.getFieldErrors()
					.stream()
					.collect(Collectors.groupingBy(FieldError::getField,
							Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	ProblemDetail handle(final RuntimeException e) {
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
