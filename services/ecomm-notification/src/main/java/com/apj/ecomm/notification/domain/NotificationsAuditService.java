package com.apj.ecomm.notification.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@Observed(name = "service.admin.notification")
@RequiredArgsConstructor
class NotificationsAuditService implements INotificationsAuditService {

	private final NotificationRepository repository;

	private final NotificationMapper mapper;

	public Paged<NotificationResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toAudit));
	}

	public NotificationResponse findById(final String id) {
		return repository.findById(id).map(mapper::toAudit).orElseThrow(ResourceNotFoundException::new);
	}

}
