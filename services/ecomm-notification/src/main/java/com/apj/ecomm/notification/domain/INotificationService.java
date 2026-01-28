package com.apj.ecomm.notification.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;

public interface INotificationService {

	void send(NotificationRequest request);

	Paged<NotificationResponse> findAllBy(String userId, Pageable pageable);

	NotificationResponse findById(String id, String userId);

}
