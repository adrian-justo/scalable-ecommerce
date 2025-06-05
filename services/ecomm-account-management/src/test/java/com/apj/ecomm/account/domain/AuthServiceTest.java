package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.IncorrectCredentialsException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private List<UserResponse> response;

	@Mock
	private UserRepository repository;

	@Spy
	private UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@Mock
	private PasswordEncoder encoder;

	@Mock
	private AuthenticationManager manager;

	@InjectMocks
	private AuthService service;

	@BeforeEach
	void setUp() throws Exception {
		ObjectMapper objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			response = objMap.readValue(inputStream, new TypeReference<List<UserResponse>>() {
			});
		}
	}

	@Test
	void register_success() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", List.of(Role.SELLER));
		User user = mapper.toEntity(request);

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.empty());
		when(repository.save(any())).thenReturn(user);

		assertEquals(Optional.of(mapper.toResponse(user)), service.register(request));
	}

	@Test
	void register_alreadyRegistered() {
		UserResponse userResponse = response.get(0);
		CreateUserRequest request = new CreateUserRequest(userResponse.username(), userResponse.email(),
				userResponse.mobileNo(), userResponse.password(), userResponse.name(), userResponse.roles());
		User existing = mapper.toEntity(request);

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.of(existing));

		assertThrows(AlreadyRegisteredException.class, () -> service.register(request));
	}

	@Test
	void login_success() {
		UserResponse existing = response.get(0);
		LoginRequest request = new LoginRequest(existing.email(), existing.password());
		Optional<User> user = Optional.of(mapper.toEntity(response.get(0)));

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString())).thenReturn(user);
		when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(new UsernamePasswordAuthenticationToken(existing.username(), existing.password()));

		assertTrue(!service.login(request).isEmpty());
	}

	@Test
	void login_incorrectCredentials() {
		LoginRequest request = new LoginRequest("nonexistent", "wrongPassword");
		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.empty());
		assertThrows(IncorrectCredentialsException.class, () -> service.login(request));
	}

}
