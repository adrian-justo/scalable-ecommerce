package com.apj.ecomm.payment.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.domain.IPaymentService;
import com.apj.ecomm.payment.domain.model.PaymentResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PaymentController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

	@Value("${api.version}${payments.path}")
	private String uri;

	private String sessionUrl;

	private String buyerId;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IPaymentService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/payments.json")) {
			final var response = mapper.readValue(inputStream, new TypeReference<List<PaymentResponse>>() {
			});
			final var payment = response.getFirst();
			sessionUrl = payment.sessionUrl();
			buyerId = payment.buyerId();
		}
	}

	@Test
	void paymentSession_found() throws Exception {
		when(service.getSession(anyString())).thenReturn(sessionUrl);
		final var action = mvc.perform(get(uri).header(AppConstants.HEADER_USER_ID, buyerId))
			.andExpect(status().isOk());
		assertEquals(sessionUrl, action.andReturn().getResponse().getContentAsString());
	}

	@Test
	void paymentSession_noHeader() throws Exception {
		mvc.perform(get(uri))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
	}

}
