package com.apj.ecomm.cart.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.ICartService;
import com.apj.ecomm.cart.domain.model.BuyerCartResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CartController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

	@Value("${api.version}${carts.path}")
	private String uri;

	private List<CartResponse> response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private ICartService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/carts.json")) {
			response = mapper.readValue(inputStream, new TypeReference<List<CartResponse>>() {});
		}
	}

	@Test
	void cartsAudit_getAll() throws Exception {
		final var result = new Paged<>(response, 0, 10, 1, List.of(), response.size());

		when(service.findAll(any(Pageable.class))).thenReturn(result);
		final var action = mvc.perform(get(uri));

		final var jsonResponse = mapper.writeValueAsString(result);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void cartDetails_getSpecific_found() throws Exception {
		final var cart = response.getFirst();

		when(service.findById(anyLong())).thenReturn(cart);
		final var action = mvc.perform(get(uri + "/1"));

		final var jsonResponse = mapper.writeValueAsString(cart);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void cartDetails_getSpecific_nonNumeric() throws Exception {
		mvc.perform(get(uri + "/nonNumeric"))
			.andExpect(
					result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
	}

	@Test
	void cartDetails_getSpecific_notValid() throws Exception {
		mvc.perform(get(uri + "/0"))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
	}

	@Test
	void cartDetails_getByBuyer() throws Exception {
		final var cartResponse = response.getFirst();
		final var buyerId = cartResponse.buyerId();
		final var cart = new BuyerCartResponse(cartResponse.id(), buyerId, List.of(), false);

		when(service.findByBuyerId(anyString())).thenReturn(cart);
		final var action = mvc.perform(get(uri + "/buyer").header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(cart);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

}
