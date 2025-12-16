package com.apj.ecomm.payment.domain;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@Profile("test")
@RequiredArgsConstructor
class PaymentDataLoader implements CommandLineRunner {

	private final ObjectMapper mapper;

	private final PaymentRepository repository;

	@Override
	public void run(final String... args) throws Exception {
		if (repository.count() == 0) {
			try (var inputStream = TypeReference.class.getResourceAsStream("/data/payments.json")) {
				repository.saveAll(mapper.readValue(inputStream, new TypeReference<List<Payment>>() {}));
			}
		}
	}

}
