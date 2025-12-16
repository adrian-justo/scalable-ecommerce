package com.apj.ecomm.payment;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

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
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.web.messaging.order.CheckoutSessionRequest;
import com.apj.ecomm.payment.web.messaging.order.OrderItemDetailResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderItemResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommPaymentProcessingApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${payments.path}")
	private String path;

	@Value("${admin.path}")
	private String adminPath;

	@LocalServerPort
	private int port;

	@Autowired
	private PostgreSQLContainer<?> postgres;

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
		assertTrue(postgres.isRunning());
	}

	// Setup valid stripe.key in application.yml to enable this test
	// @Test
	void message_createSession() {
		final var buyerId = "buyer";
		final var orders = List.of(new OrderResponse(buyerId,
				List.of(new OrderItemResponse(new OrderItemDetailResponse("image", "name", BigDecimal.TEN), 1))));

		input.send(MessageBuilder.withPayload(new CheckoutSessionRequest(orders)).build(),
				"payment-request-checkout-session");
		final var response = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertFalse(response.isEmpty());
	}

}
