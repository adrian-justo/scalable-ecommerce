package com.apj.ecomm.gateway.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.apj.ecomm.gateway.constants.Paths;
import com.apj.ecomm.gateway.security.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements WebFilter {

	private final TokenService tokenService;
	private final Paths paths;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getPath().value();

		if (paths.permitted().stream().noneMatch(s -> path.matches(s.replace("*", ".*").replace(".*.*", ".*")))) {
			String requestToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			if (requestToken != null && requestToken.startsWith("Bearer")) {
				String token = requestToken.substring(7);

				if (tokenService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
					User user = tokenService.getUser(token);

					ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
							.header("ecomm-shop-name", user.getShopName()).build();
					ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
					SecurityContext context = new SecurityContextImpl(
							new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

					return chain.filter(modifiedExchange)
							.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
				} else {
					log.error("Token is malformed or expired");
				}

			} else {
				log.warn("Token not provided in request header for path: {}", path);
			}
		}

		return chain.filter(exchange);
	}

}
