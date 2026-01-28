package com.apj.ecomm.notification.web.messaging;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.notification.domain.INotificationService;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationMessageConsumer {

	private final INotificationService service;

	@Bean
	Consumer<NotificationRequest> sendEventUpdate() {
		return service::send;
	}

}
