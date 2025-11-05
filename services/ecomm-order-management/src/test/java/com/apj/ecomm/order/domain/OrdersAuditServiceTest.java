package com.apj.ecomm.order.domain;

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

import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrdersAuditServiceTest {

	private List<Order> orders;

	@Mock
	private OrderRepository repository;

	@Spy
	private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

	@InjectMocks
	private OrdersAuditService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/orders.json")) {
			orders = objMap.readValue(inputStream, new TypeReference<List<Order>>() {
			});
		}
	}

	@Test
	void findAll() {
		final var response = orders.stream().map(mapper::toAudit).toList();
		when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(orders));
		assertEquals(new Paged<>(new PageImpl<>(response)), service.findAll(PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		final var order = orders.getFirst();
		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		assertEquals(mapper.toAudit(order), service.findById(1L));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9));
	}

}
