package com.apj.ecomm.notification.domain.model;

import com.apj.ecomm.notification.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({ @JsonSubTypes.Type(value = EmailMessage.class, name = Message.ALIAS_EMAIL),
		@JsonSubTypes.Type(value = SmsMessage.class, name = Message.ALIAS_SMS) })
public class Message {

	protected static final String ALIAS_EMAIL = "Email";

	protected static final String ALIAS_SMS = "Sms";

	private String recipient;

	private NotificationType type;

	private Long orderId;

}
