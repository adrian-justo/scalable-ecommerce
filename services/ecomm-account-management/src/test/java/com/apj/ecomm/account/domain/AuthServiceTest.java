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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.UsernameTakenException;
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
		User user = new User();
		user.setId(UUID.randomUUID());
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setMobileNo(request.mobileNo());
		user.setPassword(request.password());
		user.setName(request.name());
		user.setRoles(request.roles());

		when(repository.existsByUsernameAndActiveTrue(anyString())).thenReturn(false);
		when(repository.existsByEmailAndActiveTrueOrMobileNoAndActiveTrue(anyString(), anyString())).thenReturn(false);
		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.empty());
		when(repository.save(any())).thenReturn(user);

		assertEquals(Optional.of(mapper.toResponse(user)), service.register(request));
	}

	@Test
	void register_alreadyTaken() {
		UserResponse existing = response.get(0);
		CreateUserRequest request = new CreateUserRequest(existing.username(), existing.email(), existing.mobileNo(),
				existing.password(), existing.name(), existing.roles());
		when(repository.existsByUsernameAndActiveTrue(anyString())).thenReturn(true);
		assertThrows(UsernameTakenException.class, () -> service.register(request));
	}

	@Test
	void register_alreadyRegistered() {
		UserResponse existing = response.get(0);
		CreateUserRequest request = new CreateUserRequest(existing.username(), existing.email(), existing.mobileNo(),
				existing.password(), existing.name(), existing.roles());

		when(repository.existsByUsernameAndActiveTrue(anyString())).thenReturn(false);
		when(repository.existsByEmailAndActiveTrueOrMobileNoAndActiveTrue(anyString(), anyString())).thenReturn(true);

		assertThrows(AlreadyRegisteredException.class, () -> service.register(request));
	}

	@Test
	void login_username_success() {
		UserResponse existing = response.get(0);
		LoginRequest request = new LoginRequest(existing.username(), existing.password());
		Optional<User> user = Optional.of(mapper.toEntity(existing));

		when(repository.findByUsernameAndActiveTrue(anyString())).thenReturn(user);

		assertTrue(!service.login(request).isEmpty());
	}

	@Test
	void login_emailOrMobile_success() {
		UserResponse existing = response.get(0);
		LoginRequest request = new LoginRequest(existing.email(), existing.password());
		Optional<User> user = Optional.of(mapper.toEntity(response.get(0)));

		when(repository.findByEmailAndActiveTrueOrMobileNoAndActiveTrue(anyString(), anyString())).thenReturn(user);

		assertTrue(!service.login(request).isEmpty());
	}

	@Test
	void login_incorrectCredentials() {
		LoginRequest request = new LoginRequest("nonexistent", "wrongpassword");

		when(repository.findByUsernameAndActiveTrue(anyString())).thenReturn(Optional.empty());
		when(repository.findByEmailAndActiveTrueOrMobileNoAndActiveTrue(anyString(), anyString()))
				.thenReturn(Optional.empty());

		assertTrue(service.login(request).isEmpty());
	}

}
