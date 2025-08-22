package com.apj.ecomm.gateway.util;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

public final class ProblemDetailUtils {

	private ProblemDetailUtils() {
		// Prevent instantiation
	}

	public static ProblemDetail forStatusAndTitle(final HttpStatus status, final String title) {
		final var problemDetail = ProblemDetail.forStatus(status);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://http.dev/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	public static Mono<Void> write(final ServerWebExchange exchange, final HttpStatus status, final String title,
			final ObjectMapper mapper) {
		final var response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

		final var detail = ProblemDetailUtils.forStatusAndTitle(status, title);
		detail.setInstance(URI.create(exchange.getRequest().getURI().getPath()));
		var bytes = new byte[0];
		try {
			bytes = mapper.writeValueAsBytes(detail);
		}
		catch (final JsonProcessingException e) {
			e.printStackTrace();
			response.setStatusCode(status);
			return response.setComplete();
		}
		final var buffer = response.bufferFactory().wrap(bytes);
		return response.writeWith(Mono.just(buffer));
	}

}
