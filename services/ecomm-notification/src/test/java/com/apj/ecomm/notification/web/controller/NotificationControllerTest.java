package com.apj.ecomm.notification.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;

import com.apj.ecomm.notification.constants.AppConstants;
import com.apj.ecomm.notification.domain.INotificationService;
import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = NotificationController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

	@Value("${api.version}${notifications.path}")
	private String uri;

	private NotificationResponse response;

	private String userId;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private INotificationService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			final var notifications = mapper.readValue(inputStream, new TypeReference<List<NotificationResponse>>() {
			});
			response = notifications.getFirst();
			userId = response.userId();
		}
	}

	@Test
	void notificationHistory_getAll() throws Exception {
		final var result = new Paged<>(new PageImpl<>(List.of(response)));

		when(service.findAllBy(anyString(), any(Pageable.class))).thenReturn(result);
		final var action = mvc.perform(get(uri).header(AppConstants.HEADER_USER_ID, userId));

		final var jsonResponse = mapper.writeValueAsString(result);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void notificationDetails_getSpecific_found() throws Exception {
		when(service.findById(anyString(), anyString())).thenReturn(response);
		final var action = mvc.perform(get(uri + "/notificationId").header(AppConstants.HEADER_USER_ID, userId));

		final var jsonResponse = mapper.writeValueAsString(response);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void orderDetails_getSpecific_noHeader() throws Exception {
		mvc.perform(get(uri + "/notificationId"))
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
	}

}
