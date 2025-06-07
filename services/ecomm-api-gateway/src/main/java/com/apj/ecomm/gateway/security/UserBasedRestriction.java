package com.apj.ecomm.gateway.security;

import java.util.Map;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import com.apj.ecomm.gateway.security.model.UserResponse;

import reactor.core.publisher.Mono;

@Component
public class UserBasedRestriction implements ReactiveAuthorizationManager<AuthorizationContext> {

	private static final UriTemplate USERS_URI = new UriTemplate("/users/{username}");

	public AuthorizationDecision getDecision(Authentication authentication, AuthorizationContext context) {
		Map<String, String> uriVariables = USERS_URI.match(context.getExchange().getRequest().getPath().value());
		String requestUsername = uriVariables.get("username");
		String tokenUsername = ((UserResponse) authentication.getPrincipal()).getUsername();
		boolean hasAdminRole = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
		return new AuthorizationDecision(hasAdminRole || tokenUsername.equals(requestUsername));
	}

	@Override
	public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
		return authentication.flatMap(auth -> Mono.just(getDecision(auth, context)));
	}

}
