package com.apj.ecomm.account.domain;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	private User user;

	@InjectMocks
	private TokenService service;

	@BeforeEach
	void setUp() throws IOException {
		final var secretKey = service.generateSecret();
		System.out.println("Generated Secret Key: " + secretKey);
		ReflectionTestUtils.setField(service, "secretKey", secretKey);

		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			final var users = objMap.readValue(inputStream, new TypeReference<List<User>>() {
			});
			user = users.getFirst();
			user.setId(UUID.randomUUID());
		}
	}

	@Test
	void generationSuccessful() {
		assertFalse(service.generate(user, "").isBlank());
	}

}
