package com.apj.ecomm.gateway.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import reactor.core.publisher.Mono;

@Component
class UserBasedRestriction implements ReactiveAuthorizationManager<AuthorizationContext> {

	@Override
	public Mono<AuthorizationDecision> check(final Mono<Authentication> authentication,
			final AuthorizationContext context) {
		return authentication.map(auth -> {
			final var uri = new UriTemplate("/users/{username}");
			final var uriVariables = uri.match(context.getExchange().getRequest().getPath().value());
			final var requestUsername = uriVariables.get("username");
			final var tokenUsername = ((User) auth.getPrincipal()).getUsername();
			final var hasAdminRole = auth.getAuthorities()
				.stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
			return new AuthorizationDecision(hasAdminRole || tokenUsername.equals(requestUsername));
		});
	}

}
