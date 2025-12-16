package com.apj.ecomm.gateway.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.apj.ecomm.gateway.constants.Paths;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
class AuthFilter implements WebFilter {

	private final TokenService tokenService;

	private final Paths paths;

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
		final var request = exchange.getRequest();
		final var path = request.getPath().value();
		if (paths.permitted().stream().anyMatch(s -> path.matches(s.replace("*", ".*"))))
			return chain.filter(exchange);

		final var requestToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (requestToken == null || !requestToken.startsWith("Bearer")) {
			log.warn("Token not provided in request header for path: {}", path);
			return chain.filter(exchange);
		}

		final var token = requestToken.substring(7);
		if (!tokenService.isValid(token)) {
			log.error("Token is malformed or expired");
			return chain.filter(exchange);
		}

		final var user = tokenService.getUser(token);
		final var modifiedRequest = request.mutate()
			.header("ecomm-user-id", user.getUserId())
			.header("ecomm-shop-name", user.getShopName())
			.header("ecomm-transfer-status", user.getTransferStatus())
			.build();
		final var modifiedExchange = exchange.mutate().request(modifiedRequest).build();

		var context = SecurityContextHolder.getContext();
		if (context.getAuthentication() == null) {
			context = new SecurityContextImpl(
					new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
		}

		return chain.filter(modifiedExchange)
			.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
	}

}
