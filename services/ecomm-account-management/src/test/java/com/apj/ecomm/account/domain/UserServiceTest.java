package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.web.client.AccountClient;
import com.apj.ecomm.account.web.exception.ActiveOrderExistsException;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private User user;

	@Mock
	private UserRepository repository;

	@Mock
	private AccountClient client;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private PasswordEncoder encoder;

	@Spy
	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@InjectMocks
	private UserService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			final var users = objMap.readValue(inputStream, new TypeReference<List<User>>() {
			});
			user = users.get(1);
			user.setId(UUID.randomUUID());
		}
	}

	@Test
	void findAll() {
		final var result = new PageImpl<>(List.of(user));
		when(repository.findAll(any(PageRequest.class))).thenReturn(result);
		assertEquals(new Paged<>(result.map(mapper::toResponseNoIdentifier)), service.findAll(PageRequest.ofSize(10)));
	}

	@Test
	void findByUsername_found() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		assertEquals(mapper.toResponse(user), service.findByUsername(user.getUsername(), user.getId().toString()));
	}

	@Test
	void findByUsername_notFound() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findByUsername("nonexistent", ""));
	}

	@Test
	void update_success() {
		final var request = new UpdateUserRequest("", "+639031234567", null, null, null, null, Set.of(Role.BUYER),
				null);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(client.activeOrderExists(anyString())).thenReturn(false);
		when(repository.save(any())).thenReturn(user);
		final var updated = service.update(user.getUsername(), request);

		assertEquals(mapper.toResponse(user), updated);
		assertEquals(Set.of(NotificationType.SMS), updated.notificationTypes());
	}

	@Test
	void update_alreadyRegistered() {
		final var request = new UpdateUserRequest("client123@mail.com", "+639021234567", null, null, null, null, null,
				null);
		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.of(user));
		assertThrows(AlreadyRegisteredException.class, () -> service.update("client123", request));
	}

	@Test
	void update_userNotFound() {
		final var request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null, null,
				null);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> service.update("nonexistent", request));
	}

	@Test
	void update_activeOrderExists() {
		final var request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				Set.of(Role.BUYER), null);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(client.activeOrderExists(anyString())).thenReturn(true);

		assertThrows(ActiveOrderExistsException.class, () -> service.update("client123", request));
	}

	@Test
	void deleteByUsername_success() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(client.activeOrderExists(anyString())).thenReturn(false);
		when(repository.save(any())).thenReturn(user);

		service.deleteByUsername("admin123");
		assertFalse(user.isActive());
	}

	@Test
	void deleteByUsername_notFound() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.deleteByUsername("nonexistent"));
	}

	@Test
	void deleteByUsername_activeOrderExists() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(client.activeOrderExists(anyString())).thenReturn(true);

		assertThrows(ActiveOrderExistsException.class, () -> service.deleteByUsername("client123"));
	}

}
