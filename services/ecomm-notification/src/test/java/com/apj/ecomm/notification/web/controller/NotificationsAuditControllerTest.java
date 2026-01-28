package com.apj.ecomm.notification.web.controller;

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

import com.apj.ecomm.notification.domain.INotificationsAuditService;
import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = NotificationsAuditController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class NotificationsAuditControllerTest {

	@Value("${api.version}${admin.path}${notifications.path}")
	private String uri;

	private List<NotificationResponse> response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private INotificationsAuditService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			response = mapper.readValue(inputStream, new TypeReference<List<NotificationResponse>>() {
			});
		}
	}

	@Test
	void notificationsAudit_getAll() throws Exception {
		final var result = new Paged<>(new PageImpl<>(response));

		when(service.findAll(any(Pageable.class))).thenReturn(result);
		final var action = mvc.perform(get(uri));

		final var jsonResponse = mapper.writeValueAsString(result);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void notificationsAudit_getSpecific() throws Exception {
		final var result = response.getFirst();

		when(service.findById(anyString())).thenReturn(result);
		final var action = mvc.perform(get(uri + "/notificationId"));

		final var jsonResponse = mapper.writeValueAsString(result);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

}
