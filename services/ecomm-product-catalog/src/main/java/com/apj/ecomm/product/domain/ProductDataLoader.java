package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ProductDataLoader implements CommandLineRunner {

	private final ObjectMapper mapper;

	private final ProductRepository repository;

	@Override
	public void run(final String... args) throws Exception {
		if (repository.count() == 0) {
			try (var inputStream = TypeReference.class.getResourceAsStream("/data/products.json")) {
				repository.saveAll(mapper.readValue(inputStream, new TypeReference<List<Product>>() {}));
			}
		}

	}

}
