package com.apj.ecomm.notification.config;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.apj.ecomm.notification.domain.NotificationType;
import com.apj.ecomm.notification.domain.model.EmailMessage;
import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.domain.model.SmsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@ReadingConverter
public class MessageConverter implements Converter<Document, Message> {

	public Message convert(final Document source) {
		final var type = NotificationType.valueOf(source.getString("type"));
		final var mapper = new ObjectMapper();
		return switch (type) {
			case EMAIL -> mapper.convertValue(source, EmailMessage.class);
			case SMS -> mapper.convertValue(source, SmsMessage.class);
			default -> throw new IllegalArgumentException("Unknown class type: " + type);
		};
	}

}
