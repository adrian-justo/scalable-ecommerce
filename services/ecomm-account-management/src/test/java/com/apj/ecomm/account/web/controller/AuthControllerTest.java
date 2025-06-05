package com.apj.ecomm.account.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.apj.ecomm.account.domain.IAuthService;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class, properties = { "eureka.client.enabled=false",
		"spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	private final String uri = "/api/v1/auth";

	private List<UserResponse> response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IAuthService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			response = mapper.readValue(inputStream, new TypeReference<List<UserResponse>>() {
			});
		}
	}

	@Test
	void userRegistration_success() throws Exception {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", List.of(Role.SELLER));
		UserResponse userResponse = new UserResponse(request.username(), request.email(), request.mobileNo(),
				request.password(), request.name(), "", "", request.roles(), List.of(NotificationType.EMAIL), true);

		when(service.register(any())).thenReturn(Optional.of(userResponse));
		ResultActions action = mvc.perform(post(uri + "/register").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)));

		String jsonResponse = mapper.writeValueAsString(userResponse);
		action.andExpect(status().isCreated()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void userRegistration_invalidDetails() throws Exception {
		CreateUserRequest request = new CreateUserRequest("", "", "", "", "", null);
		mvc.perform(post(uri + "/register").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void login_success() throws Exception {
		UserResponse userResponse = response.get(0);
		LoginRequest request = new LoginRequest(userResponse.username(), userResponse.password());
		String jwt = "jwt.token.here";

		when(service.login(any())).thenReturn(Optional.of(jwt));
		mvc.perform(post(uri + "/login").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))).andExpect(status().isOk());
	}

}
