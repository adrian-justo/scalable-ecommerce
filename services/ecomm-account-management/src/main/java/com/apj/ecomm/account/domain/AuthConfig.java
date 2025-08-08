package com.apj.ecomm.account.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class AuthConfig {

	private final UserRepository repository;

	@Bean
	UserDetailsService userDetailsService() {
		return identifier -> repository.findByUsernameOrEmailOrMobileNo(identifier, identifier, identifier)
			.filter(User::isActive)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

}
