package com.apj.ecomm.notification.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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

import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.notification.web.exception.ResourceNotFoundException;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	private Notification notification;

	private String userId;

	private Message message;

	@Mock
	private Map<String, NotificationProvider> providers;

	@Mock
	private NotificationRepository repository;

	@Spy
	private final NotificationMapper mapper = Mappers.getMapper(NotificationMapper.class);

	@InjectMocks
	private NotificationService service;

	@Mock
	private NotificationProvider provider;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			final var notifications = objMap.readValue(inputStream, new TypeReference<List<Notification>>() {
			});
			notification = notifications.getFirst();
			userId = notification.getUserId();
			message = notification.getMessage();
		}
	}

	@Test
	void findAllBy() {
		final var result = new PageImpl<>(List.of(notification));
		when(repository.findAllByUserId(anyString(), any(PageRequest.class))).thenReturn(result);
		assertEquals(new Paged<>(result.map(mapper::toResponse)), service.findAllBy(userId, PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		when(repository.findById(anyString())).thenReturn(Optional.of(notification));
		assertEquals(mapper.toResponse(notification), service.findById("notificationId", userId));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById("nonExisting", userId));
	}

	@Test
	void findById_accessDenied() {
		when(repository.findById(anyString())).thenReturn(Optional.of(notification));
		assertThrows(ResourceAccessDeniedException.class, () -> service.findById("notificationId", "anotherUser"));
	}

	@Test
	void send() {
		final var request = new NotificationRequest(1L, userId, Role.SELLER, "recipient", NotificationType.EMAIL,
				Status.CONFIRMED);

		when(providers.get(any())).thenReturn(provider);
		when(provider.generate(any(NotificationRequest.class), any(NotificationMapper.class))).thenReturn(message);
		when(provider.send(any(Message.class))).thenReturn(null);

		service.send(request);
		verify(repository, times(1)).save(notification);
	}

}
