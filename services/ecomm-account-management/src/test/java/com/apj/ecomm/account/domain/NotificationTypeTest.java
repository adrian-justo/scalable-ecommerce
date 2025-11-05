package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class NotificationTypeTest {

	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@Test
	void validate_emailOnlyNotif_hasMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.EMAIL)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.EMAIL), user.getNotificationTypes());
	}

	@Test
	void validate_smsOnlyNotif_hasEmail() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.SMS)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.SMS), user.getNotificationTypes());
	}

	@Test
	void validate_emailOnlyNotif_noEmail() {
		final var user = new User();
		user.setMobileNo("+639031234567");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.EMAIL)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.SMS), user.getNotificationTypes());
	}

	@Test
	void validate_smsOnlyNotif_noMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.SMS)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.EMAIL), user.getNotificationTypes());
	}

	@Test
	void validate_emailSmsNotif_noMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.SMS, NotificationType.EMAIL)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.EMAIL), user.getNotificationTypes());
	}

	@Test
	void validate_emailSmsNotif_noEmail() {
		final var user = new User();
		user.setMobileNo("+639031234567");
		user.setNotificationTypes(new HashSet<>(Set.of(NotificationType.SMS, NotificationType.EMAIL)));

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.SMS), user.getNotificationTypes());
	}

	@Test
	void validate_noNotif_hasEmailMobile() {
		final var user = new User();
		user.setEmail("seller123@mail.com");
		user.setMobileNo("+639031234567");

		mapper.validate(user);
		assertEquals(Set.of(NotificationType.EMAIL), user.getNotificationTypes());
	}

}
