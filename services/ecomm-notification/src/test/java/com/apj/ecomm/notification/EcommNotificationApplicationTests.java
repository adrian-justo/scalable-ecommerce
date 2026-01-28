package com.apj.ecomm.notification;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;

import com.apj.ecomm.notification.domain.NotificationType;
import com.apj.ecomm.notification.domain.model.EmailMessage;
import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.domain.model.SmsMessage;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;
import com.apj.ecomm.notification.web.messaging.order.Role;
import com.apj.ecomm.notification.web.messaging.order.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommNotificationApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${notifications.path}")
	private String path;

	@Value("${admin.path}")
	private String adminPath;

	@LocalServerPort
	private int port;

	@Autowired
	private MongoDBContainer mongoDB;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private InputDestination input;

	@BeforeAll
	static void setUpBeforeClass() {
		RestAssured.baseURI = "http://localhost";
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Test
	void connectionEstablished() {
		assertTrue(mongoDB.isRunning());
	}

	@Test
	void sendEmail() throws JsonProcessingException {
		final var request = new NotificationRequest(1L, "userId", Role.BUYER, "user@email.com", NotificationType.EMAIL,
				Status.INACTIVE);
		final var notification = send(request);
		assertEquals(request.userId(), notification.userId());
		assertInstanceOf(EmailMessage.class, notification.message());
	}

	@Test
	void sendSms() throws JsonProcessingException {
		final var request = new NotificationRequest(1L, "userId", Role.SELLER, "+639087654322", NotificationType.SMS,
				Status.CONFIRMED);
		final var notification = send(request);
		assertEquals(request.userId(), notification.userId());
		assertInstanceOf(SmsMessage.class, notification.message());
	}

	private NotificationResponse send(final NotificationRequest request) throws JsonProcessingException {
		input.send(MessageBuilder.withPayload(request).build(), "notification-send-event-update");
		final var response = when().get(apiVersion + adminPath + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		return mapper.readValue(response, new TypeReference<Paged<NotificationResponse>>() {
		}).result().getLast();
	}

}
