package com.apj.ecomm.account.web.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.RequestArgumentNotValidException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class, properties = { "eureka.client.enabled=false",
		"spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private final String uri = "/api/v1/users";

	private List<UserResponse> response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IUserService service;

	@BeforeEach
	void setUp() throws IOException {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			response = mapper.readValue(inputStream, new TypeReference<List<UserResponse>>() {
			});
		}

	}

	@Test
	void accountDetails_getAll() throws Exception {
		when(service.findAll(anyInt(), anyInt())).thenReturn(response);
		ResultActions action = mvc.perform(get(uri));

		String jsonResponse = mapper.writeValueAsString(response);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void accountDetails_getSpecific() throws Exception {
		UserResponse userResponse = response.get(0);

		when(service.findByUsername(anyString())).thenReturn(Optional.of(userResponse));
		ResultActions action = mvc.perform(get(uri + "/admin123"));

		String jsonResponse = mapper.writeValueAsString(userResponse);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void accountManagement_success() throws Exception {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				null, null);
		UserResponse user = response.get(0);
		UserResponse userResponse = new UserResponse(user.username(), request.email(), request.mobileNo(),
				user.password(), user.name(), user.shopName(), user.address(), user.roles(), user.notificationTypes(),
				user.active());

		when(service.update(anyString(), any())).thenReturn(Optional.of(userResponse));
		ResultActions action = mvc.perform(
				put(uri + "/admin123").contentType("application/json").content(mapper.writeValueAsString(request)));

		String jsonResponse = mapper.writeValueAsString(userResponse);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void accountManagement_invalidDetails() throws Exception {
		UpdateUserRequest request = new UpdateUserRequest("updatedemail.com", "0", null, null, null, null, null, null);
		mvc.perform(put(uri + "/admin123").contentType("application/json").content(mapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void accountManagement_emailSmsMissing() {
		UpdateUserRequest request = new UpdateUserRequest("", "", null, null, null, null, null, null);
		assertThrows(RequestArgumentNotValidException.class, request::validate);
	}

	@Test
	void accountManagement_invalidRole() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				Set.of(Role.ADMIN, Role.BUYER), null);
		assertThrows(RequestArgumentNotValidException.class, request::validate);
	}

	@Test
	void accountManagement_invalidNotificationType() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				null, Set.of());
		assertThrows(RequestArgumentNotValidException.class, request::validate);
	}

	@Test
	void accountDeletion_success() throws Exception {
		doNothing().when(service).deleteByUsername(anyString());
		mvc.perform(delete(uri + "/admin123")).andExpect(status().isNoContent());
		verify(service, times(1)).deleteByUsername("admin123");
	}

}
