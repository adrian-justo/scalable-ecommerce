package com.apj.ecomm.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final AuthFilter authFilter;
	private final AuthEntryPoint authEntryPoint;
	private final UserBasedRestriction userBased;

	private static final String[] ROLES = { "ADMIN", "BUYER", "SELLER" };
	private static final String[] PERMITTED_PATHS = { "/fallback/**", "/v3/api-docs/**", "/swagger-ui.html",
			"/swagger-ui/**", "/webjars/**", "/actuator/**", "/api/*/auth/**" };
	private static final String[] ADMIN_ONLY_PATHS = { "/api/*/users" };
	private static final String[] BUYER_ONLY_PATHS = { "" };
	private static final String[] SELLER_ONLY_PATHS = { "" };
	private static final String[] NON_SELLER_PATHS = { "" };
	private static final String[] NON_BUYER_PATHS = { "" };
	private static final String[] USER_RESTRICTED_PATHS = { "/api/*/users/**" };

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
				.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.pathMatchers(PERMITTED_PATHS)
						.permitAll().pathMatchers(ADMIN_ONLY_PATHS).hasRole(ROLES[0]).pathMatchers(BUYER_ONLY_PATHS)
						.hasRole(ROLES[1]).pathMatchers(SELLER_ONLY_PATHS).hasRole(ROLES[2])
						.pathMatchers(NON_SELLER_PATHS).hasAnyRole(ROLES[0], ROLES[1]).pathMatchers(NON_BUYER_PATHS)
						.hasAnyRole(ROLES[0], ROLES[2]).pathMatchers(USER_RESTRICTED_PATHS).access(userBased)
						.anyExchange().authenticated())
				.addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION).httpBasic(HttpBasicSpec::disable)
				.formLogin(FormLoginSpec::disable).build();
	}

}
