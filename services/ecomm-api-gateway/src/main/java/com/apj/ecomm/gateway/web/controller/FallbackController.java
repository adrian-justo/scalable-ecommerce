package com.apj.ecomm.gateway.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.apj.ecomm.gateway.util.ProblemDetailUtils;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ProblemDetail fallback(final ServerWebExchange exchange) {
		return ProblemDetailUtils.forStatusAndTitle(HttpStatus.SERVICE_UNAVAILABLE,
				"Service is currently unavailable. Please try again later.");
	}

}
