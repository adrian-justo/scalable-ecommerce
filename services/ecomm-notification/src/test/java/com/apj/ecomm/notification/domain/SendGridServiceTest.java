package com.apj.ecomm.notification.domain;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
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

import com.apj.ecomm.notification.domain.model.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@ExtendWith(MockitoExtension.class)
class SendGridServiceTest {

	private Message message;

	@Mock
	private SendGrid sendGrid;

	@Spy
	private final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	private SendGridService service;

	@Mock
	private Response response;

	@BeforeEach
	void setUp() throws Exception {
		ReflectionTestUtils.setField(service, "from", "no_reply@company.com");
		ReflectionTestUtils.setField(service, "sender", "Company");

		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/notifications.json")) {
			final var notifications = mapper.readValue(inputStream, new TypeReference<List<Notification>>() {
			});
			message = notifications.getFirst().getMessage();
		}
	}

	@Test
	void send_successful() throws IOException {
		when(sendGrid.api(any(Request.class))).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.OK.value());
		assertNull(service.send(message));
	}

	@Test
	void send_unsuccessful() throws IOException {
		when(sendGrid.api(any(Request.class))).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST.value());
		when(response.getBody()).thenReturn(HttpStatus.BAD_REQUEST.getReasonPhrase());
		assertNotNull(service.send(message));
	}

}
