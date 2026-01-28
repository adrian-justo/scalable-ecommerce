package com.apj.ecomm.notification.domain;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.apj.ecomm.notification.domain.model.EmailMessage;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Component(EmailNotificationProvider.CURRENT)
@Observed(name = "provider.email.sendgrid")
@RequiredArgsConstructor
class SendGridService extends EmailNotificationProvider {

	@Value("${sender.email.address}")
	private String from;

	@Value("${sender.email.name}")
	private String sender;

	private final SendGrid sendGrid;

	private final ObjectMapper mapper;

	String getTemplateId(final Status status, final Role role) {
		return switch (status) {
			case INACTIVE -> "d-af00";
			case CONFIRMED -> role == Role.SELLER ? "d-af01" : "d-af02";
			default -> throw new IllegalArgumentException("Unexpected value: " + status);
		};
	}

	String sendEmail(final EmailMessage message) {
		try {
			final var response = sendGrid.api(sendMail(createFrom(message)));
			final var status = HttpStatus.valueOf(response.getStatusCode());
			return status.is2xxSuccessful() ? null : response.getBody();
		}
		catch (final IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private Mail createFrom(final EmailMessage message) {
		final var mail = new Mail();
		mail.setFrom(new Email(from, sender));
		mail.setTemplateId(message.getTemplateId());
		mail.addPersonalization(getTemplateData(message));
		return mail;
	}

	private Personalization getTemplateData(final EmailMessage message) {
		final var personalization = new Personalization();
		personalization.addTo(new Email(message.getRecipient()));
		mapper.convertValue(message, new TypeReference<Map<String, Object>>() {
		}).forEach(personalization::addDynamicTemplateData);
		return personalization;
	}

	private Request sendMail(final Mail mail) throws IOException {
		final var request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());
		return request;
	}

}
