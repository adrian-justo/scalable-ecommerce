package com.apj.ecomm.order.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.domain.IOrderService;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = OrderController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	@Value("${api.version}${orders.path}")
	private String uri;

	private OrderResponse response;

	private String buyerId;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IOrderService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/orders.json")) {
			final var orderResponse = mapper.readValue(inputStream, new TypeReference<List<OrderResponse>>() {
			});
			response = orderResponse.getFirst();
			buyerId = response.buyerId();
		}
	}

	@Test
	void orderHistory_getAll() throws Exception {
		final var result = new Paged<>(new PageImpl<>(List.of(response)));

		when(service.findAllBy(anyString(), any(PageRequest.class))).thenReturn(result);
		final var action = mvc.perform(get(uri).header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(result);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void orderDetails_getSpecific_found() throws Exception {
		when(service.findById(anyLong(), anyString())).thenReturn(response);
		final var action = mvc.perform(get(uri + "/1").header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(response);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void orderDetails_getSpecific_noHeader() throws Exception {
		mvc.perform(get(uri + "/1"))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
	}

	@Test
	void orderDetails_getSpecific_nonNumeric() throws Exception {
		mvc.perform(get(uri + "/nonNumeric").header(AppConstants.HEADER_USER_ID, buyerId))
			.andExpect(
					result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
	}

	@Test
	void orderDetails_getSpecific_notValid() throws Exception {
		mvc.perform(get(uri + "/0").header(AppConstants.HEADER_USER_ID, buyerId))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
	}

	@Test
	void checkOut() throws Exception {
		final var orders = List.of(response);

		when(service.checkOut(anyString(), any())).thenReturn(orders);
		final var action = mvc.perform(post(uri).header(AppConstants.HEADER_USER_ID, buyerId));

		final var jsonResponse = mapper.writeValueAsString(orders);
		action.andExpect(status().isCreated()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

}
