package com.apj.ecomm.gateway.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.apj.ecomm.gateway.constants.Paths;
import com.apj.ecomm.gateway.constants.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final AuthFilter authFilter;
	private final AuthEntryPoint authEntryPoint;
	private final UserBasedRestriction ubRestriction;
	private final Paths paths;

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
				.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
						.pathMatchers(convert(paths.permitted())).permitAll().pathMatchers(convert(paths.adminOnly()))
						.hasRole(Role.ADMIN.name()).pathMatchers(convert(paths.buyerOnly())).hasRole(Role.BUYER.name())
						.pathMatchers(convert(paths.sellerOnly())).hasRole(Role.SELLER.name())
						.pathMatchers(convert(paths.nonSeller())).hasAnyRole(Role.ADMIN.name(), Role.BUYER.name())
						.pathMatchers(convert(paths.nonBuyer())).hasAnyRole(Role.ADMIN.name(), Role.SELLER.name())
						.pathMatchers(convert(paths.userBased())).access(ubRestriction).anyExchange().authenticated())
				.addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION).httpBasic(HttpBasicSpec::disable)
				.formLogin(FormLoginSpec::disable).build();
	}

	private String[] convert(List<String> paths) {
		return paths.toArray(new String[0]);
	}

}
