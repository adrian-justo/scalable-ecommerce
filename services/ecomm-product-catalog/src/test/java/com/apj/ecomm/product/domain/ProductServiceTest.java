package com.apj.ecomm.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	private List<Product> products;

	@Mock
	private ProductRepository repository;

	@Mock
	private ProductSpecBuilder specBuilder;

	@Spy
	private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

	@InjectMocks
	private ProductService service;

	@BeforeEach
	void setUp() throws Exception {
		ObjectMapper objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/products.json")) {
			products = objMap.readValue(inputStream, new TypeReference<List<Product>>() {
			});
		}
	}

	@Test
	void findAll() {
		Page<Product> page = new PageImpl<>(products);
		List<ProductCatalog> catalog = page.stream().map(mapper::toCatalog).toList();

		when(specBuilder.build(any())).thenReturn(null);
		when(repository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(PageRequest.class)))
				.thenReturn(page);

		assertEquals(catalog, service.findAll("", PageRequest.of(0, 10)));
	}

	@Test
	void findById_found() {
		Product product = products.get(0);
		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		assertEquals(mapper.toResponse(product), service.findById(1));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9));
	}

	@Test
	void getStock() {
		Product product = products.get(0);
		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		assertEquals(product.getStock(), service.getStock(1));
	}

	@Test
	void save() {
		CreateProductRequest request = new CreateProductRequest("name", "description", Set.of("image-uri"),
				Set.of("category"), 1, BigDecimal.ONE);
		Product product = mapper.toEntity(request);
		when(repository.save(any())).thenReturn(product);
		assertEquals(mapper.toResponse(product), service.list("Shop Name", request));
	}

	@Test
	void update() {
		UpdateProductRequest request = new UpdateProductRequest(null, "description", null, null, 1, BigDecimal.ONE);
		Product existing = products.get(0);
		Product product = mapper.updateEntity(request, existing);

		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		when(repository.save(any())).thenReturn(product);

		assertEquals(mapper.toResponse(product), service.update(1, request));
	}

}
