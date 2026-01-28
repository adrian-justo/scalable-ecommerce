package com.apj.ecomm.notification.domain;

import com.apj.ecomm.notification.domain.model.EmailMessage;
import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;

abstract class EmailNotificationProvider implements NotificationProvider {

	static final String CURRENT = "sendGridService";

	public Message generate(final NotificationRequest request, final NotificationMapper mapper) {
		return mapper.toEmail(request, getTemplateId(request.status(), request.role()));
	}

	abstract String getTemplateId(final Status status, final Role role);

	public String send(final Message message) {
		return sendEmail((EmailMessage) message);
	}

	abstract String sendEmail(EmailMessage message);

}