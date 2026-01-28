package com.apj.ecomm.notification.domain;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.notification.web.exception.ResourceNotFoundException;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.notification")
@RequiredArgsConstructor
class NotificationService implements INotificationService {

	private final Map<String, NotificationProvider> providers;

	private final NotificationRepository repository;

	private final NotificationMapper mapper;

	public Paged<NotificationResponse> findAllBy(final String userId, final Pageable pageable) {
		return new Paged<>(repository.findAllByUserId(userId, pageable).map(mapper::toResponse));
	}

	public NotificationResponse findById(final String id, final String userId) {
		final var result = repository.findById(id);
		if (result.isEmpty())
			throw new ResourceNotFoundException();
		return result.filter(notification -> userId.equals(notification.getUserId()))
			.map(mapper::toResponse)
			.orElseThrow(ResourceAccessDeniedException::new);
	}

	public void send(final NotificationRequest request) {
		final var provider = providers.get(NotificationProvider.getBy(request.type()));
		final var message = provider.generate(request, mapper);
		final var error = provider.send(message);
		repository.save(mapper.toEntity(request, message, error == null, error));
	}

}
