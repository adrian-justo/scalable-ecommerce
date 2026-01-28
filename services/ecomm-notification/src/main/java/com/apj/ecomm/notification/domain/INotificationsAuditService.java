package com.apj.ecomm.notification.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;

public interface INotificationsAuditService {

	Paged<NotificationResponse> findAll(Pageable pageable);

	NotificationResponse findById(String id);

}
