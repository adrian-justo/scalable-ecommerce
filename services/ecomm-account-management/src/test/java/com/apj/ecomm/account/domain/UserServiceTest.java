package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.InputStream;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
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
	private UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@InjectMocks
	private UserService service;

	@BeforeEach
	void setUp() throws Exception {
		ObjectMapper objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			users = objMap.readValue(inputStream, new TypeReference<List<User>>() {
			});
		}
	}

	@Test
	void findAll() {
		Page<User> page = new PageImpl<>(users);
		List<UserResponse> response = page.stream().map(mapper::toResponse).toList();
		when(repository.findAll(any(PageRequest.class))).thenReturn(page);
		assertEquals(response, service.findAll(PageRequest.of(0, 10)));
	}

	@Test
	void findByUsername_found() {
		User user = users.get(0);
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
		UpdateUserRequest request = new UpdateUserRequest("", "+639031234567", null, null, null, null, null, null);
		User existing = users.get(1);
		User updated = mapper.updateEntity(request, existing);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenReturn(updated);
		UserResponse userResponse = service.update("client123", request);

		assertEquals(mapper.toResponse(updated), userResponse);
		assertEquals(Set.of(NotificationType.SMS), userResponse.notificationTypes());
	}

	@Test
	void update_userNotFound() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				null, null);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.empty());
		when(repository.findByUsername(anyString())).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> service.update("nonexistent", request));
	}

	@Test
	void update_alreadyRegistered() {
		UpdateUserRequest request = new UpdateUserRequest("client123@mail.com", "+639021234567", null, null, null, null,
				null, null);
		User existing = users.get(1);

		when(repository.findByEmailOrMobileNo(anyString(), anyString())).thenReturn(Optional.of(existing));

		assertThrows(AlreadyRegisteredException.class, () -> service.update("admin123", request));
	}

	@Test
	void getValidatedTypes_emailOnlyNotif_hasMobile() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				null, Set.of(NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_hasEmail() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639031234567", null, null, null, null,
				null, Set.of(NotificationType.SMS));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_emailOnlyNotif_noEmail() {
		UpdateUserRequest request = new UpdateUserRequest("", "+639031234567", null, null, null, null, null,
				Set.of(NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_noMobile() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "", null, null, null, null, null,
				Set.of(NotificationType.SMS));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noMobile() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "", null, null, null, null, null,
				Set.of(NotificationType.SMS, NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noEmail() {
		UpdateUserRequest request = new UpdateUserRequest("", "+639031234567", null, null, null, null, null,
				Set.of(NotificationType.SMS, NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_nullNotif_hasEmail() {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "", null, null, null, null, null, null);
		Set<NotificationType> types = service.getValidatedTypes(mapper.updateEntity(request, users.get(1)));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void deleteByUsername_success() {
		User user = users.get(0);

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
