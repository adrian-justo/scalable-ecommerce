package com.apj.ecomm.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.client.OrderClient;
import com.apj.ecomm.order.web.client.cart.CartItemResponse;
import com.apj.ecomm.order.web.exception.OrderStillProcessingException;
import com.apj.ecomm.order.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	private Order order;

	private String buyerId;

	@Mock
	private OrderRepository repository;

	@Mock
	private OrderClient client;

	@Spy
	private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private OrderService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/orders.json")) {
			final var orders = objMap.readValue(inputStream, new TypeReference<List<Order>>() {
			});
			order = orders.getFirst();
			buyerId = order.getBuyerId();
		}
	}

	@Test
	void findAllBy() {
		final var result = new PageImpl<>(List.of(order));
		when(repository.findAllByBuyerIdAndStatusNot(anyString(), anyString(), any(PageRequest.class)))
			.thenReturn(result);
		assertEquals(new Paged<>(result.map(mapper::toResponse)), service.findAllBy(buyerId, PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		assertEquals(mapper.toResponse(order), service.findById(1L, buyerId));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9, buyerId));
	}

	@Test
	void findById_accessDenied() {
		when(repository.findById(anyLong())).thenReturn(Optional.of(order));
		assertThrows(ResourceAccessDeniedException.class, () -> service.findById(1L, "anotherBuyer"));
	}

	@Test
	void checkOut_success() {
		final var cart = List.of(new CartItemResponse(1L, "SHP001", 1));

		when(repository.existsByBuyerIdAndStatus(anyString(), anyString())).thenReturn(false);
		when(client.getAllCartItems(anyString())).thenReturn(cart);
		when(repository.findAllByBuyerIdAndStatusIn(anyString(), ArgumentMatchers.<List<String>>any()))
			.thenReturn(List.of());
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of(order));

		assertEquals(List.of(mapper.toResponse(order)), service.checkOut(buyerId, null));
	}

	@Test
	void checkOut_processingOrders() {
		when(repository.existsByBuyerIdAndStatus(anyString(), anyString())).thenReturn(true);
		assertThrows(OrderStillProcessingException.class, () -> service.checkOut(buyerId, null));
	}

	@Test
	void checkOut_noItems() {
		when(repository.existsByBuyerIdAndStatus(anyString(), anyString())).thenReturn(false);
		when(client.getAllCartItems(anyString())).thenReturn(List.of());
		assertThrows(ResourceNotFoundException.class, () -> service.checkOut(buyerId, null));
	}

	@Test
	void updateInformationAndGetStockUpdate() {
		final var delInfo = order.getDeliveryInformation();
		final var shopInfo = order.getShopInformation();
		final var userInformation = Map.of(buyerId,
				new UserResponse(delInfo.getName(), null, delInfo.getAddress(), delInfo.getEmail(), null,
						delInfo.getNotificationTypes()),
				order.getShopId(), new UserResponse(null, shopInfo.getName(), shopInfo.getAddress(), null,
						shopInfo.getMobileNo(), shopInfo.getNotificationTypes()));

		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of(order));

		assertEquals(
				Optional.of(order.getProducts()
					.stream()
					.collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity))),
				service.updateInformationAndGetProducts(buyerId, userInformation));
	}

	@Test
	void updateInformationAndGetStockUpdate_inactiveSeller() {
		final var delInfo = order.getDeliveryInformation();
		final var userInformation = Map.of(buyerId, new UserResponse(delInfo.getName(), null, delInfo.getAddress(),
				delInfo.getEmail(), null, delInfo.getNotificationTypes()));

		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of());

		assertTrue(service.updateInformationAndGetProducts(buyerId, userInformation).isEmpty());
	}

	@Test
	void populateDetailAndRequestCheckout() {
		final var details = order.getProducts()
			.stream()
			.collect(Collectors.toMap(OrderItem::getProductId,
					item -> new ProductResponse(item.getProductDetail().getName(), order.getShopId(),
							List.of(item.getProductDetail().getImage()), 1, item.getProductDetail().getPrice())));

		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of(order));

		order.setStatus(Status.ACTIVE.toString());
		assertEquals(Optional.of(List.of(mapper.toResponse(order))),
				service.populateDetailAndGetOrders(buyerId, details));
	}

	@Test
	void populateOrderItemDetail_outOfStock() {
		final var details = order.getProducts()
			.stream()
			.collect(Collectors.toMap(OrderItem::getProductId,
					item -> new ProductResponse(item.getProductDetail().getName(), order.getShopId(),
							List.of(item.getProductDetail().getImage()), 0, item.getProductDetail().getPrice())));

		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of());

		assertTrue(service.populateDetailAndGetOrders(buyerId, details).isEmpty());
	}

	@Test
	void updateStatusAndGetDetails() {
		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of(order));
		assertEquals(Optional.of(Map.of(order.getShopId(), order.getTotal())),
				service.updateStatusAndGetDetails(buyerId, Status.CONFIRMED));
	}

	@Test
	void updateStatusAndGetDetails_inactive() {
		when(repository.findAllByBuyerIdAndStatus(anyString(), anyString())).thenReturn(List.of(order));
		when(repository.saveAll(ArgumentMatchers.<List<Order>>any())).thenReturn(List.of(order));
		assertTrue(service.updateStatusAndGetDetails(buyerId, Status.INACTIVE).isEmpty());
	}

}
