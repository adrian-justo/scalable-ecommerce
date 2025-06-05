package com.apj.ecomm.account.domain;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AccountDataLoader implements CommandLineRunner {

	private final ObjectMapper mapper;
	private final UserRepository repository;
	private final PasswordEncoder encoder;

	@Override
	public void run(String... args) throws Exception {
		if (repository.count() == 0) {
			try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
				List<User> users = mapper.readValue(inputStream, new TypeReference<List<User>>() {});
				users.forEach(user -> user.setPassword(encoder.encode(user.getPassword())));
				repository.saveAll(users);
			}
		}

	}

}
