package com.apj.ecomm.cart.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
import org.springframework.data.domain.Pageable;

import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	private List<CartItem> cartItems;

	private Cart cart;

	private final String buyerId = "client123";

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository repository;

	@Mock
	private CartClient client;

	@Spy
	private final CartItemMapper mapper = Mappers.getMapper(CartItemMapper.class);

	@Spy
	@InjectMocks
	private final CartMapper cartMapper = Mappers.getMapper(CartMapper.class);

	@InjectMocks
	private CartService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/carts.json")) {
			final var carts = objMap.readValue(inputStream, new TypeReference<List<Cart>>() {
			});
			cart = carts.getFirst();
			cartItems = cart.getProducts();
		}
	}

	@Test
	void findCartByBuyerId() {
		final var products = cart.getProducts()
			.stream()
			.map(p -> new ProductResponse(p.getProductId(), null, null, null, null, null, null, null, null))
			.toList();

		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(client.getAllProducts(anyString(), any(Pageable.class))).thenReturn(new Paged<>(new PageImpl<>(products)));

		assertEquals(products.size(), service.findCartBy(buyerId).products().size());
	}

	@Test
	void findItemsByBuyerId() {
		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		assertEquals(cartItems.stream().map(mapper::toResponse).toList(), service.findItemsBy(buyerId));
	}

	@Test
	void findItemByProductId_found() {
		final var item = cartItems.getFirst();
		final var product = new ProductResponse(item.getProductId(), null, null, null, null, null, null, null,
				BigDecimal.ONE);

		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(client.getAllProducts(anyString(), any(Pageable.class)))
			.thenReturn(new Paged<>(new PageImpl<>(List.of(product))));

		assertEquals(mapper.toDetail(item, product), service.findItemBy(product.id(), buyerId));
	}

	@Test
	void findItemByProductId_notFound() {
		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		assertThrows(ResourceNotFoundException.class, () -> service.findItemBy(1L, buyerId));
	}

	@Test
	void addItems() {
		final var item1 = cartItems.getFirst();
		final var product1 = new ProductResponse(item1.getProductId(), null, item1.getShopId(), null, null, null, null,
				2, BigDecimal.ONE);
		final var item2 = new CartItem();
		item2.setProductId(3L);
		item2.setQuantity(2);
		final var product2 = new ProductResponse(item2.getProductId(), null, item2.getShopId(), null, null, null, null,
				item2.getQuantity() - 1, BigDecimal.TWO);
		final var request = List.of(new CartItemRequest(item1.getProductId()),
				new CartItemRequest(item2.getProductId(), item2.getQuantity()), new CartItemRequest(4L));
		item2.setQuantity(product2.stock());

		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(client.getAllProducts(anyString(), any(Pageable.class)))
			.thenReturn(new Paged<>(new PageImpl<>(List.of(product1, product2))));
		when(repository.saveAll(ArgumentMatchers.<List<CartItem>>any())).thenReturn(List.of(item1, item2));

		final var expected = List.of(
				new CartItemResponse(null, item1.getProductId(), item1.getShopId(), item1.getQuantity() + 1, null,
						null),
				new CartItemResponse(null, item2.getProductId(), item2.getShopId(), product2.stock(), null, null));
		assertEquals(expected, service.addItems(buyerId, request));
	}

	@Test
	void updateItems() {
		final var item = cartItems.getFirst();
		final var product = new ProductResponse(item.getProductId(), null, item.getShopId(), null, null, null, null, 3,
				BigDecimal.ONE);
		final var requestItem = new CartItemRequest(item.getProductId(), 3);
		item.setQuantity(requestItem.quantity());
		final var request = List.of(requestItem);

		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(client.getAllProducts(anyString(), any(Pageable.class)))
			.thenReturn(new Paged<>(new PageImpl<>(List.of(product))));
		when(repository.saveAll(ArgumentMatchers.<List<CartItem>>any())).thenReturn(List.of(item));

		final var expected = List.of(new CartItemResponse(null, item.getProductId(), item.getShopId(), 3, null, null));
		assertEquals(expected, service.updateItems(buyerId, request));
	}

	@Test
	void updateItemsFromEvent() {
		final var item1 = cartItems.getFirst();
		final var item2 = cartItems.get(1);
		final var products = Map.of(item1.getProductId(), 1, item2.getProductId(), 0);

		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(repository.saveAll(ArgumentMatchers.<List<CartItem>>any())).thenReturn(List.of(item1));
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);

		final var expected = List.of(new CartItemResponse(null, item1.getProductId(), item1.getShopId(),
				products.get(item1.getProductId()), null, null));
		assertEquals(expected, service.updateItemsFromEvent(buyerId, products));
	}

	@Test
	void deleteItems() {
		when(cartRepository.findAllByBuyerId(anyString())).thenReturn(List.of(cart));
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);

		service.deleteItems(buyerId, List.of(1L, 2L));
		verify(cartRepository, times(1)).save(cart);
	}

}
