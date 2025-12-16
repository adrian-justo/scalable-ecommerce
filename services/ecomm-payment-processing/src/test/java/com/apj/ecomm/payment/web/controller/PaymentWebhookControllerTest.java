package com.apj.ecomm.payment.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.apj.ecomm.payment.domain.IPaymentWebhookService;

@WebMvcTest(controllers = PaymentWebhookController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

	@Value("${api.version}${webhooks.path}${payments.path}")
	private String uri;

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private IPaymentWebhookService service;

	@Test
	void handleWebhookEvent() throws Exception {
		mvc.perform(
				post(uri).header("signature", "signature").contentType("application/json").content("{\"payload\":1}"))
			.andExpect(status().isOk());
	}

}
