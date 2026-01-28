package com.apj.ecomm.notification.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.apj.ecomm.notification.domain.model.SmsMessage;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component(SMSNotificationProvider.CURRENT)
@Observed(name = "provider.sms.twilio")
@RequiredArgsConstructor
class TwilioService extends SMSNotificationProvider {

	@Value("${twilio.sid}")
	private String sid;

	@Value("${twilio.key}")
	private String key;

	@Value("${sender.sms}")
	private String from;

	private final ObjectMapper mapper;

	@PostConstruct
	void init() {
		Twilio.init(sid, key);
	}

	String getContentSid(final Status status, final Role role) {
		return switch (status) {
			case INACTIVE -> "HXbc00";
			case CONFIRMED -> role == Role.SELLER ? "HXbc01" : "HXbc02";
			default -> throw new IllegalArgumentException("Unexpected value: " + status);
		};
	}

	String sendSms(final SmsMessage message) {
		try {
			final var sms = Message.creator(new PhoneNumber(message.getRecipient()), new PhoneNumber(from), "")
				.setContentSid(message.getContentSid())
				.setContentVariables(mapper.writeValueAsString(message))
				.create();
			return sms.getErrorMessage();
		}
		catch (final ApiException | JsonProcessingException e) {
			return e.getMessage();
		}
	}

}
