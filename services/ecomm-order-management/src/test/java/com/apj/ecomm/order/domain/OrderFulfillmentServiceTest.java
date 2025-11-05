package com.apj.ecomm.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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

import com.apj.ecomm.order.domain.model.CompleteOrderRequest;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderFulfillmentServiceTest {

	private Order order;

	private String shopId;

	@Mock
	private OrderRepository repository;

	@Spy
	private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

	@InjectMocks
	private OrderFulfillmentService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/orders.json")) {
			final var orders = objMap.readValue(inputStream, new TypeReference<List<Order>>() {
			});
			order = orders.getFirst();
			shopId = order.getShopId();
		}
	}

	@Test
	void activeOrderExists() {
		when(repository.existsByShopIdAndStatusIn(anyString(), ArgumentMatchers.<List<String>>any())).thenReturn(true);
		assertTrue(service.activeOrderExists(shopId));
	}

	@Test
	void findAllBy() {
		final var result = new PageImpl<>(List.of(order));
		when(repository.findAllByShopIdAndStatusNot(anyString(), anyString(), any(PageRequest.class)))
			.thenReturn(result);
		assertEquals(new Paged<>(result.map(mapper::toResponse)), service.findAllBy(shopId, PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		assertEquals(mapper.toResponse(order), service.findById(1L, shopId));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9, shopId));
	}

	@Test
	void findById_accessDenied() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		assertThrows(ResourceAccessDeniedException.class, () -> service.findById(1L, "anotherBuyer"));
	}

	@Test
	void update() {
		final var request = new CompleteOrderRequest("trackingNumber");
		order.setTrackingNumber(request.trackingNumber());
		order.setStatus(Status.CONFIRMED.toString());

		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		when(repository.save(any(Order.class))).thenReturn(order);

		service.update(1L, shopId, request);
		assertEquals(request.trackingNumber(), order.getTrackingNumber());
	}

}
