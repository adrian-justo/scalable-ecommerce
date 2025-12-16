package com.apj.ecomm.account.web.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.apj.ecomm.account.domain.IAuthService;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.RequestArgumentNotValidException;
import com.apj.ecomm.account.web.util.RequestValidator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Value("${api.version}${auth.path}")
	private String uri;

	private UserResponse response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IAuthService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			final var list = mapper.readValue(inputStream, new TypeReference<List<UserResponse>>() {
			});
			response = list.getFirst();
		}
	}

	@Test
	void userRegistration_success() throws Exception {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "Seller's Shop", "Seller's Address", Set.of(Role.SELLER),
				Set.of(NotificationType.EMAIL));

		when(service.register(any())).thenReturn(response);
		final var action = mvc.perform(post(uri + "register").contentType(MediaType.APPLICATION_JSON)
			.content(mapper.writeValueAsString(request)));

		final var jsonResponse = mapper.writeValueAsString(response);
		action.andExpect(status().isCreated()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void userRegistration_invalidDetails() throws Exception {
		final var request = new CreateUserRequest("", "", "", "", "", "", "", null, null);
		mvc.perform(post(uri + "register").contentType(MediaType.APPLICATION_JSON)
			.content(mapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void userRegistration_emailSmsMissing() {
		final var request = new CreateUserRequest("seller123", "", "", "$elL3r12", "Seller Name", "Seller's Shop",
				"Seller's Address", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		assertThrows(RequestArgumentNotValidException.class, () -> RequestValidator.validate(request));
	}

	@Test
	void userRegistration_invalidRole() {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "Seller's Shop", "Seller's Address", Set.of(Role.ADMIN, Role.SELLER),
				Set.of(NotificationType.EMAIL));
		assertThrows(RequestArgumentNotValidException.class, () -> RequestValidator.validate(request));
	}

	@Test
	void userRegistration_seller_noShopDetails() {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "", "", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		assertThrows(RequestArgumentNotValidException.class, () -> RequestValidator.validate(request));
	}

	@Test
	void userRegistration_invalidNotificationType() {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "Seller's Shop", "Seller's Address", Set.of(Role.SELLER), Set.of());
		assertThrows(RequestArgumentNotValidException.class, () -> RequestValidator.validate(request));
	}

	@Test
	void login_success() throws Exception {
		final var request = new LoginRequest(response.username(), "password");

		when(service.login(any())).thenReturn("jwt.token.here");
		mvc.perform(
				post(uri + "login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

}
