package com.apj.ecomm.cart.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

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

	@InjectMocks
	private CartItemService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/carts.json")) {
			final var carts = objMap.readValue(inputStream, new TypeReference<List<Cart>>() {});
			cart = carts.getFirst();
			cartItems = cart.getProducts();
		}
	}

	@Test
	void findAll() {
		final var catalog = new PageImpl<>(
				List.of(new ProductCatalog(1L, null, null, null), new ProductCatalog(2L, null, null, null)));

		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(repository.findAllByCartId(anyLong())).thenReturn(cartItems);
		when(client.getAllProducts(anyString(), any(PageRequest.class))).thenReturn(new Paged<>(catalog));

		assertEquals(mapper.toCatalog(cartItems, catalog.getContent()), service.findAll(1L, buyerId));
	}

	@Test
	void findById_found() {
		final var item = cartItems.getFirst();
		final var product = new ProductResponse(1L, null, null, null, null, null, null, null, BigDecimal.ONE);

		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(repository.findById(any(CartItemId.class))).thenReturn(Optional.of(item));
		when(client.getProductById(anyLong())).thenReturn(product);

		assertEquals(mapper.toDetail(item, product), service.findById(1L, buyerId, product.id()));
	}

	@Test
	void findById_accessDenied() {
		when(cartRepository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceAccessDeniedException.class, () -> service.findById(1L, buyerId, 1L));
	}

	@Test
	void findById_notFound() {
		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(repository.findById(any(CartItemId.class))).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(1L, buyerId, 1L));
	}

	@Test
	void addAll() {
		final var item1 = cartItems.getFirst();
		final var item2 = new CartItem();
		item2.setProductId(3L);
		item2.setQuantity(1);
		final var catalog = new PageImpl<>(List.of(new ProductCatalog(item1.getProductId(), null, null, null),
				new ProductCatalog(item2.getProductId(), null, null, null)));
		final var request = List.of(new CartItemRequest(item1.getProductId()),
				new CartItemRequest(item2.getProductId()), new CartItemRequest(5L, 2));
		final var expected = List.of(new CartItemResponse(item1.getProductId(), item1.getQuantity() + 1),
				new CartItemResponse(item2.getProductId(), item2.getQuantity()));

		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(client.getAllProducts(anyString(), any(PageRequest.class))).thenReturn(new Paged<>(catalog));
		when(repository.findAllByCartId(anyLong())).thenReturn(cartItems);
		when(repository.saveAll(ArgumentMatchers.<List<CartItem>>any())).thenReturn(List.of(item1, item2));

		assertEquals(expected, service.addAll(1L, buyerId, request));
	}

	@Test
	void updateAll() {
		final var item = cartItems.getFirst();
		final var request = List.of(new CartItemRequest(item.getProductId(), 3), new CartItemRequest(3L, 2));
		final var expected = List.of(new CartItemResponse(item.getProductId(), 3));

		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(repository.findAllByCartId(anyLong())).thenReturn(cartItems);
		when(repository.saveAll(ArgumentMatchers.<List<CartItem>>any())).thenReturn(List.of(item));

		assertEquals(expected, service.updateAll(1L, buyerId, request));
	}

	@Test
	void deleteAll() {
		when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
		when(repository.findAllByCartId(anyLong())).thenReturn(cartItems);
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		doNothing().when(repository).deleteAll(ArgumentMatchers.<Iterable<CartItem>>any());
		service.deleteAll(1L, buyerId, List.of(1L, 2L));
		verify(repository, times(1)).deleteAll(ArgumentMatchers.<Iterable<CartItem>>any());
	}

}
