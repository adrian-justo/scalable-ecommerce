package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AccountServiceTest {

	private final IAccountService service = new IAccountService() {
		// This is a dummy implementation for testing purposes
	};

	@Test
	void getValidatedTypes_emailOnlyNotif_hasMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");
		assertEquals(Set.of(NotificationType.EMAIL),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.EMAIL))));
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_hasEmail() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");
		assertEquals(Set.of(NotificationType.SMS),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.SMS))));
	}

	@Test
	void getValidatedTypes_emailOnlyNotif_noEmail() {
		final var user = new User();
		user.setMobileNo("+639031234567");
		assertEquals(Set.of(NotificationType.SMS),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.EMAIL))));
	}

	@Test
	void getValidatedTypes_smsOnlyNotif_noMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		assertEquals(Set.of(NotificationType.EMAIL),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.SMS))));
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		assertEquals(Set.of(NotificationType.EMAIL),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.SMS, NotificationType.EMAIL))));
	}

	@Test
	void getValidatedTypes_emailSmsNotif_noEmail() {
		final var user = new User();
		user.setMobileNo("+639031234567");
		assertEquals(Set.of(NotificationType.SMS),
				service.getValidatedTypes(user, new HashSet<>(Set.of(NotificationType.SMS, NotificationType.EMAIL))));
	}

	@Test
	void getValidatedTypes_nullNotif_hasEmailMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");
		assertEquals(Set.of(NotificationType.EMAIL), service.getValidatedTypes(user, new HashSet<>()));
	}

}
