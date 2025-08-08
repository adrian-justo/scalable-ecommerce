package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private List<User> users;

	@Mock
	private UserRepository repository;

	@Spy
	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@Mock
	private PasswordEncoder encoder;

	@Mock
	private AuthenticationManager manager;

	@Mock
	private TokenService token;

	@InjectMocks
	private AuthService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			users = objMap.readValue(inputStream, new TypeReference<List<User>>() {});
		}
	}

	@Test
	void register_success() {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "Seller's Shop", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		final var user = mapper.toEntity(request);

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
			.thenReturn(Optional.empty());
		when(repository.save(any())).thenReturn(user);

		assertEquals(mapper.toResponse(user), service.register(request));
	}

	@Test
	void register_alreadyRegistered() {
		final var user = users.get(1);
		final var request = new CreateUserRequest(user.getUsername(), user.getEmail(), user.getMobileNo(),
				user.getPassword(), user.getName(), user.getShopName(), user.getRoles(), user.getNotificationTypes());

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
			.thenReturn(Optional.of(user));

		assertThrows(AlreadyRegisteredException.class, () -> service.register(request));
	}

	@Test
	void login_success() {
		final var user = users.get(0);
		final var request = new LoginRequest(user.getEmail(), user.getPassword());

		when(manager.authenticate(any()))
			.thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
		when(token.generate(any())).thenReturn("jwt.token.here");

		assertTrue(!service.login(request).isEmpty());
	}

	@Test
	void login_incorrectCredentials() {
		final var request = new LoginRequest("nonexistent", "wrongPassword");
		when(manager.authenticate(any()))
			.thenReturn(UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password()));
		assertThrows(BadCredentialsException.class, () -> service.login(request));
	}

}
