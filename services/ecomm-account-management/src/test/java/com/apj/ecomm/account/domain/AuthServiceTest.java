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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

	private User user;

	@Mock
	private UserRepository repository;

	@Mock
	private PasswordEncoder encoder;

	@Mock
	private AuthenticationManager manager;

	@Mock
	private TokenService token;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private PaymentProcessor processor;

	@Spy
	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@InjectMocks
	private AuthService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			final var users = objMap.readValue(inputStream, new TypeReference<List<User>>() {
			});
			user = users.get(1);
		}
	}

	@Test
	void register_success() {
		final var request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567", "$elL3r12",
				"Seller Name", "Seller's Shop", "Seller's Address", Set.of(Role.SELLER),
				Set.of(NotificationType.EMAIL));
		final var saved = mapper.toEntity(request, encoder);
		saved.setId(UUID.randomUUID());

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
			.thenReturn(Optional.empty());
		when(processor.create()).thenReturn(user.getAccountId());
		when(repository.save(any())).thenReturn(saved);

		assertEquals(mapper.toResponse(saved), service.register(request));
	}

	@Test
	void register_alreadyRegistered() {
		final var request = new CreateUserRequest(user.getUsername(), user.getEmail(), user.getMobileNo(),
				user.getPassword(), user.getName(), user.getShopName(), user.getAddress(), user.getRoles(),
				user.getNotificationTypes());

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
			.thenReturn(Optional.of(user));

		assertThrows(AlreadyRegisteredException.class, () -> service.register(request));
	}

	@Test
	void login_success() {
		final var request = new LoginRequest(user.getEmail(), user.getPassword());
		user.setId(UUID.randomUUID());

		when(manager.authenticate(any()))
			.thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
		when(processor.transferEnabledFor(anyString())).thenReturn(true);
		when(processor.getTransferStatus(anyString())).thenReturn("active");
		when(token.generate(any(), anyString())).thenReturn("jwt.token.here");

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
