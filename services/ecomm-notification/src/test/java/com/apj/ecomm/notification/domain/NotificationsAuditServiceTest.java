package com.apj.ecomm.notification.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NotificationsAuditServiceTest {

	private List<Notification> notifications;

	@Mock
	private NotificationRepository repository;

	@Spy
	private final NotificationMapper mapper = Mappers.getMapper(NotificationMapper.class);

	@InjectMocks
	private NotificationsAuditService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			notifications = objMap.readValue(inputStream, new TypeReference<List<Notification>>() {
			});
		}
	}

	@Test
	void findAll() {
		final var response = notifications.stream().map(mapper::toAudit).toList();
		when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(notifications));
		assertEquals(new Paged<>(new PageImpl<>(response)), service.findAll(PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		final var notification = notifications.getFirst();
		when(repository.findById(anyString())).thenReturn(Optional.of(notification));
		assertEquals(mapper.toAudit(notification), service.findById("notificationId"));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById("notFound"));
	}

}
