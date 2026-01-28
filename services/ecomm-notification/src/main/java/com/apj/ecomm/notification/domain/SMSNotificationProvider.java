package com.apj.ecomm.notification.domain;

import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.domain.model.SmsMessage;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;

abstract class SMSNotificationProvider implements NotificationProvider {

	static final String CURRENT = "twilioService";

	public Message generate(final NotificationRequest request, final NotificationMapper mapper) {
		return mapper.toSms(request, getContentSid(request.status(), request.role()));
	}

	abstract String getContentSid(Status status, Role role);

	public String send(final Message message) {
		return sendSms((SmsMessage) message);
	}

	abstract String sendSms(SmsMessage message);

}