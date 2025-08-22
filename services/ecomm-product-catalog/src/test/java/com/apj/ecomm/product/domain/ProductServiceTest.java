package com.apj.ecomm.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.Paged;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceAccessDeniedException;
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
	private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

	@InjectMocks
	private ProductService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/products.json")) {
			products = objMap.readValue(inputStream, new TypeReference<List<Product>>() {});
		}
	}

	@Test
	void findAll() {
		final var response = products.stream().map(mapper::toCatalog).toList();
		final var result = new Paged<>(response, 0, products.size(), 1, List.of(), response.size());

		when(specBuilder.build(any())).thenReturn(null);
		when(repository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(PageRequest.class)))
			.thenReturn(new PageImpl<>(products));

		assertEquals(result, service.findAll("", PageRequest.of(0, 10)));
	}

	@Test
	void findById_found() {
		final var product = products.get(0);
		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		assertEquals(mapper.toResponse(product), service.findById(1));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9));
	}

	@Test
	void list() {
		final var request = new CreateProductRequest("name", "description", List.of(AppConstants.IMAGE_DEFAULT),
				Set.of("category"), 1, BigDecimal.ONE);
		final var product = mapper.toEntity(request);
		when(repository.save(any())).thenReturn(product);
		assertEquals(mapper.toResponse(product), service.list("SHP001", "Shop Name", request));
	}

	@Test
	void update_success() {
		final var request = new UpdateProductRequest(null, "description", null, null, 1, BigDecimal.ONE);
		final var existing = products.get(0);
		final var product = mapper.updateEntity(request, existing);

		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		when(repository.save(any())).thenReturn(product);

		assertEquals(mapper.toResponse(product), service.update(1, "SHP001", request));
	}

	@Test
	void update_accessDenied() {
		final var request = new UpdateProductRequest(null, "description", null, null, 1, BigDecimal.ONE);
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceAccessDeniedException.class, () -> service.update(1, "SHP001", request));
	}

	@Test
	void getProductsBy() {
		final var productIds = products.stream().map(Product::getId).toList();
		when(repository.findAllByShopId(anyString())).thenReturn(products);
		assertEquals(productIds, service.getProductsBy("SHP001"));
	}

	@Test
	void updateShopName() {
		final var product = products.get(0);

		when(repository.findById(anyLong())).thenReturn(Optional.of(product));
		product.setShopName("New Shop Name");
		when(repository.save(any())).thenReturn(product);

		assertEquals(mapper.toResponse(product), service.update("New Shop Name", 1L));
	}

	@Test
	void updateShopName_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.update("New Shop Name", 1L));
	}

}
