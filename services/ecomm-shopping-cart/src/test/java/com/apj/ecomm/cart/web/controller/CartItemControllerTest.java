package com.apj.ecomm.cart.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.ICartItemService;
import com.apj.ecomm.cart.domain.model.CartItemCatalog;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CartItemController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class CartItemControllerTest {

	@Value("${api.version}${carts.path}/1")
	private String uri;

	@Value("${products.path}")
	private String productsPath;

	private List<CartItemResponse> response;

	private final String buyerId = "client123";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private ICartItemService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/carts.json")) {
			final var carts = mapper.readValue(inputStream, new TypeReference<List<CartResponse>>() {});
			response = carts.getFirst().products();
		}
	}

	@Test
	void cartItems() throws Exception {
		final var catalog = response.stream()
			.map(item -> new CartItemCatalog(new ProductCatalog(item.productId(), null, null, BigDecimal.ONE),
					item.quantity()))
			.toList();

		when(service.findAll(anyLong(), anyString())).thenReturn(catalog);
		final var action = mvc.perform(get(uri + productsPath).header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(catalog);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void cartItemDetails_getSpecific_found() throws Exception {
		final var product = new ProductResponse(1L, "Item 1", "SHP001", "Shop 1", "Description 1", List.of("image1"),
				Set.of("category1"), 1, BigDecimal.ONE);
		final var cartItem = new CartItemDetail(product, response.stream().findFirst().get().quantity());

		when(service.findById(anyLong(), anyString(), anyLong())).thenReturn(cartItem);
		final var action = mvc.perform(get(uri + productsPath + "/1").header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(cartItem);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void cartItemDetails_getSpecific_noHeader() throws Exception {
		mvc.perform(get(uri + productsPath + "/1"))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
	}

	@Test
	void cartItemDetails_getSpecific_nonNumeric() throws Exception {
		mvc.perform(get(uri + productsPath + "/nonNumeric").header(AppConstants.HEADER_USER_ID, buyerId))
			.andExpect(
					result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
	}

	@Test
	void cartItemDetails_getSpecific_notValid() throws Exception {
		mvc.perform(get(uri + productsPath + "/0").header(AppConstants.HEADER_USER_ID, buyerId))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
	}

	@Test
	void addToCart() throws Exception {
		final var request = List.of(new CartItemRequest(1L));
		final var item = List.of(new CartItemResponse(1L, 3));

		when(service.addAll(anyLong(), anyString(), ArgumentMatchers.<List<CartItemRequest>>any())).thenReturn(item);
		final var action = mvc.perform(post(uri + productsPath).header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(mapper.writeValueAsString(request)));

		final var jsonResponse = mapper.writeValueAsString(item);
		action.andExpect(status().isCreated()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void updateQuantity() throws Exception {
		final var request = List.of(new CartItemRequest(1L, 3));
		final var item = List.of(new CartItemResponse(1L, 3));

		when(service.updateAll(anyLong(), anyString(), ArgumentMatchers.<List<CartItemRequest>>any())).thenReturn(item);
		final var action = mvc.perform(put(uri + productsPath).header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(mapper.writeValueAsString(request)));

		final var jsonResponse = mapper.writeValueAsString(item);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void removeItems() throws Exception {
		doNothing().when(service).deleteAll(anyLong(), anyString(), ArgumentMatchers.<List<Long>>any());
		mvc.perform(delete(uri + productsPath).header(AppConstants.HEADER_USER_ID, buyerId).queryParam("id", "1", "2"))
			.andExpect(status().isNoContent());
		verify(service, times(1)).deleteAll(1L, buyerId, List.of(1L, 2L));
	}

}
