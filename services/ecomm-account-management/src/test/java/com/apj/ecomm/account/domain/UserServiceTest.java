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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private List<User> users;

	@Mock
	private UserRepository repository;

	@Spy
	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@InjectMocks
	private UserService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			users = objMap.readValue(inputStream, new TypeReference<List<User>>() {});
		}
	}

	@Test
	void findAll() {
		final var response = users.stream().map(mapper::toFullResponse).toList();
		final var result = new Paged<>(response, 0, users.size(), 1, List.of(), response.size());
		when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(users));
		assertEquals(result, service.findAll(PageRequest.of(0, 10)));
	}

	@Test
	void findByUsername_found() {
		final var user = users.get(0);
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		assertEquals(mapper.toResponse(user), service.findByUsername("admin123"));
	}

	@Test
	void findByUsername_notFound() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findByUsername("nonexistent"));
	}

	@Test
	void update_success() {
		final var request = new UpdateUserRequest("", "+639031234567", null, null, null, null, null, null);
		final var existing = users.get(1);
		final var updated = mapper.updateEntity(request, existing);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenReturn(updated);
		final var userResponse = service.update("client123", request);

		assertEquals(mapper.toResponse(updated), userResponse);
		assertEquals(Set.of(NotificationType.SMS), userResponse.notificationTypes());
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
	void update_alreadyRegistered() {
		final var request = new UpdateUserRequest("client123@mail.com", "+639021234567", null, null, null, null, null,
				null);
		final var existing = users.get(1);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.of(existing));

		assertThrows(AlreadyRegisteredException.class, () -> service.update("admin123", request));
	}

	@Test
	void deleteByUsername_success() {
		final var user = users.get(0);

		when(repository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(repository.save(any())).thenReturn(user);
		service.deleteByUsername("admin123");

		assertFalse(user.isActive());
	}

	@Test
	void deleteByUsername_notFound() {
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.deleteByUsername("nonexistent"));
	}

}
