package com.apj.ecomm.cart.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CartsAuditServiceTest {

	private List<Cart> carts;

	@Mock
	private CartRepository repository;

	@Spy
	private final CartItemMapper itemMapper = Mappers.getMapper(CartItemMapper.class);

	@Spy
	@InjectMocks
	private final CartMapper mapper = Mappers.getMapper(CartMapper.class);

	@InjectMocks
	private CartsAuditService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/carts.json")) {
			carts = objMap.readValue(inputStream, new TypeReference<List<Cart>>() {
			});
		}
	}

	@Test
	void findAll() {
		final var result = new PageImpl<>(carts.stream().map(mapper::toResponse).toList());
		when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(carts));
		assertEquals(new Paged<>(result), service.findAll(PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		final var result = new PageImpl<>(
				List.of(new ProductResponse(1L, null, null, null, null, null, null, null, null),
						new ProductResponse(2L, null, null, null, null, null, null, null, null)));
		when(repository.findById(anyLong())).thenReturn(Optional.of(carts.getFirst()));
		assertEquals(result.getSize(), service.findById(1).products().size());
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9));
	}

}
