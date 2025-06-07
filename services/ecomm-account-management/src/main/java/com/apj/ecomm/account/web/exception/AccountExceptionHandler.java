package com.apj.ecomm.account.web.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AccountExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ProblemDetail handle(UserNotFoundException e) {
		return getDetail(HttpStatus.NOT_FOUND, e.getMessage(), "User is not found");
	}

	@ExceptionHandler(AlreadyRegisteredException.class)
	public ProblemDetail handle(AlreadyRegisteredException e) {
		return getDetail(HttpStatus.CONFLICT, e.getMessage(),
				"Username, email, and/or mobile number has already been registered");
	}

	@ExceptionHandler(IncorrectCredentialsException.class)
	public ProblemDetail handle(IncorrectCredentialsException e) {
		return getDetail(HttpStatus.UNAUTHORIZED, e.getMessage(), "Credentials provided is incorrect");
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ProblemDetail handle(BadCredentialsException e) {
		return getDetail(HttpStatus.UNAUTHORIZED, e.getMessage(), "Credentials provided is incorrect");
	}

	@ExceptionHandler(EmailSmsMissingException.class)
	public ProblemDetail handle(EmailSmsMissingException e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage(),
				"Please provide at least one of email or mobile number");
	}

	@ExceptionHandler(InvalidRoleException.class)
	public ProblemDetail handle(InvalidRoleException e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage(), "Roles set is invalid");
	}

	@ExceptionHandler(InvalidNotificationTypeException.class)
	public ProblemDetail handle(InvalidNotificationTypeException e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage(), "Notification type set is invalid");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handle(MethodArgumentNotValidException e) {
		return getDetail(HttpStatus.BAD_REQUEST, e.getMessage(), "Details provided is invalid");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handle(Exception e) {
		return getDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
	}

	private ProblemDetail getDetail(HttpStatus status, String message, String title) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}
}
