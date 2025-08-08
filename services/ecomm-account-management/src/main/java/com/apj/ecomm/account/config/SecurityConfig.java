package com.apj.ecomm.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService userDetailsService;

	@Bean
	SecurityFilterChain filterChain(final HttpSecurity security) throws Exception {
		// Allowed all requests since security is already handled by the API Gateway
		return security.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}

	@Bean
	AuthenticationProvider authenticationProvider() {
		final var provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
