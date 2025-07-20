package com.apj.ecomm.gateway.web.controller;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ProblemDetail fallback(ServerWebExchange exchange) {
		return getDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service is currently unavailable. Please try again later.");
	}

	private ProblemDetail getDetail(HttpStatus status, String title) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(status);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

}
