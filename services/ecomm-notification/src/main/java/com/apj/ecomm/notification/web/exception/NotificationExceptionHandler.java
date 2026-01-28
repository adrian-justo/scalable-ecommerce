package com.apj.ecomm.notification.web.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.notification.constants.AppConstants;

@RestControllerAdvice
class NotificationExceptionHandler {

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

}
