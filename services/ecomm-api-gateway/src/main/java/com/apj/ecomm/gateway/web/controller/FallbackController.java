package com.apj.ecomm.gateway.web.controller;

import java.net.URI;
import java.time.Instant;

import org.apache.commons.lang.WordUtils;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping(value = "/{segment}")
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ProblemDetail fallback(@PathVariable String segment, ServerWebExchange exchange) {
		Throwable t = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
		return getDetail(HttpStatus.SERVICE_UNAVAILABLE, t != null ? t.getMessage() : "Unknown error",
				WordUtils.capitalizeFully(segment.replace("-", " ")) + " service is currently unavailable");
	}

	private ProblemDetail getDetail(HttpStatus status, String message, String title) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

}
