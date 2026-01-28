package com.apj.ecomm.notification.domain;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

@ExtendWith(MockitoExtension.class)
class TwilioServiceTest {

	private com.apj.ecomm.notification.domain.model.Message message;

	@Spy
	private final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	private TwilioService service;

	@Mock
	private MessageCreator creator;

	@Mock
	private Message msg;

	@BeforeEach
	void setUp() throws Exception {
		ReflectionTestUtils.setField(service, "sid", "ACabc12E");
		ReflectionTestUtils.setField(service, "key", "abc12E");
		ReflectionTestUtils.setField(service, "from", "+12345678901");

		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			final var notifications = mapper.readValue(inputStream, new TypeReference<List<Notification>>() {
			});
			message = notifications.getLast().getMessage();
		}
	}

	@Test
	void send_successful() {
		try (final var mock = mockStatic(Message.class)) {
			mock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
				.thenReturn(creator);
			when(creator.setContentSid(anyString())).thenReturn(creator);
			when(creator.setContentVariables(anyString())).thenReturn(creator);
			when(creator.create()).thenReturn(msg);
			when(msg.getErrorMessage()).thenReturn(null);
			assertNull(service.send(message));
		}
	}

	@Test
	void send_unsuccessful() {
		try (final var mock = mockStatic(Message.class)) {
			mock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
				.thenReturn(creator);
			when(creator.setContentSid(anyString())).thenReturn(creator);
			when(creator.setContentVariables(anyString())).thenReturn(creator);
			when(creator.create()).thenReturn(msg);
			when(msg.getErrorMessage()).thenReturn(HttpStatus.BAD_REQUEST.getReasonPhrase());
			assertNotNull(service.send(message));
		}
	}

}
