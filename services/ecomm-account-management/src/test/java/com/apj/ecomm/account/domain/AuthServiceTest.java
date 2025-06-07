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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.EmailSmsMissingException;
import com.apj.ecomm.account.web.exception.IncorrectCredentialsException;
import com.apj.ecomm.account.web.exception.InvalidNotificationTypeException;
import com.apj.ecomm.account.web.exception.InvalidRoleException;
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

	@Mock
	private TokenService token;

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
				"$elL3r12", "Seller Name", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		User user = mapper.toEntity(request);

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.empty());
		when(repository.save(any())).thenReturn(user);

		assertEquals(Optional.of(mapper.toResponse(user)), service.register(request));
	}

	@Test
	void register_alreadyRegistered() {
		UserResponse user = response.get(1);
		CreateUserRequest request = new CreateUserRequest(user.username(), user.email(), user.mobileNo(),
				user.password(), user.name(), user.roles(), user.notificationTypes());
		User existing = mapper.toEntity(request);

		when(repository.findByUsernameOrEmailOrMobileNo(anyString(), anyString(), anyString()))
				.thenReturn(Optional.of(existing));

		assertThrows(AlreadyRegisteredException.class, () -> service.register(request));
	}

	@Test
	void register_emailSmsMissing() {
		CreateUserRequest request = new CreateUserRequest("seller123", "", "", "$elL3r12", "Seller Name",
				Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		assertThrows(EmailSmsMissingException.class, () -> service.register(request));
	}

	@Test
	void register_invalidRole() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", Set.of(Role.ADMIN, Role.SELLER), Set.of(NotificationType.EMAIL));
		assertThrows(InvalidRoleException.class, () -> service.register(request));
	}

	@Test
	void register_invalidNotificationType() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", Set.of(Role.SELLER), Set.of());
		assertThrows(InvalidNotificationTypeException.class, () -> service.register(request));
	}

	@Test
	void getValidatedTypes_emailOnlyNotif_hasMobile() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_hasEmail() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", Set.of(Role.SELLER), Set.of(NotificationType.SMS));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_emailOnlyNotif_noEmail() {
		CreateUserRequest request = new CreateUserRequest("seller123", null, "+639031234567", "$elL3r12", "Seller Name",
				Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_noMobile() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", null, "$elL3r12",
				"Seller Name", Set.of(Role.SELLER), Set.of(NotificationType.SMS));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noMobile() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", null, "$elL3r12",
				"Seller Name", Set.of(Role.SELLER), Set.of(NotificationType.SMS, NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noEmail() {
		CreateUserRequest request = new CreateUserRequest("seller123", null, "+639031234567", "$elL3r12", "Seller Name",
				Set.of(Role.SELLER), Set.of(NotificationType.SMS, NotificationType.EMAIL));
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.SMS), types);
	}

	@Test
	void getValidatedTypes_nullNotif_hasEmailMobile() {
		CreateUserRequest request = new CreateUserRequest("seller123", "seller123@mail.com", "+639031234567",
				"$elL3r12", "Seller Name", Set.of(Role.SELLER), null);
		Set<NotificationType> types = service.getValidatedTypes(mapper.toEntity(request));
		assertEquals(Set.of(NotificationType.EMAIL), types);
	}

	@Test
	void login_success() {
		UserResponse existing = response.get(0);
		LoginRequest request = new LoginRequest(existing.email(), existing.password());
		User user = mapper.toEntity(existing);

		when(manager.authenticate(any()))
				.thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
		when(token.generate(any())).thenReturn("jwt.token.here");

		assertTrue(!service.login(request).isEmpty());
	}

	@Test
	void login_incorrectCredentials() {
		LoginRequest request = new LoginRequest("nonexistent", "wrongPassword");
		when(manager.authenticate(any())).thenReturn(
				UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password()));
		assertThrows(IncorrectCredentialsException.class, () -> service.login(request));
	}

}
