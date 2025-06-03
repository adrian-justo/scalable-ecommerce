package com.apj.ecomm.account.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
class AccountDataLoader implements CommandLineRunner {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private UserRepository repository;

	@Override
	public void run(String... args) throws Exception {
		if (repository.count() == 0) {
			try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
				List<User> users = mapper.readValue(inputStream, new TypeReference<List<User>>() {});
				repository.saveAll(users);
			} catch (IOException e) {
				throw new RuntimeException("Failed to read JSON data", e);
			}
		}

	}

}
