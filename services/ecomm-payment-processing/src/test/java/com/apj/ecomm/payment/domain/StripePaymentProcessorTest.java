package com.apj.ecomm.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.domain.model.PaymentEvents;
import com.apj.ecomm.payment.web.messaging.order.OrderItemDetailResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderItemResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;
import com.apj.ecomm.payment.web.messaging.order.Status;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionListParams;

@Disabled
@ExtendWith(MockitoExtension.class)
class StripePaymentProcessorTest {

	private static final String STRIPE_MOCK = "http://localhost:12111";

	private final HttpHeaders headers = new HttpHeaders(
			MultiValueMap.fromSingleValue(Map.of("Stripe-Signature", "signature")));

	private final String buyerId = "buyerId";

	private final Map<String, String> metadata = Map.of(AppConstants.HEADER_USER_ID, buyerId);

	@Spy
	private final StripeMapper mapper = Mappers.getMapper(StripeMapper.class);

	@InjectMocks
	private StripePaymentProcessor processor;

	@Mock
	private Event event;

	@Mock
	private EventDataObjectDeserializer deserializer;

	@Mock
	private Optional<StripeObject> object;

	@BeforeAll
	static void setUpBeforeClass() {
		Stripe.overrideApiBase(STRIPE_MOCK);
	}

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(processor, "stripeKey", "sk_test_123");
		ReflectionTestUtils.setField(processor, "successUrl", STRIPE_MOCK + "/success");
		ReflectionTestUtils.setField(processor, "webhookSecret", "whsec_123");
		processor.setKey();
	}

	@Test
	void getValue() {
		assertEquals(SessionListParams.Status.OPEN.getValue(), processor.getValue(SessionStatus.OPEN));
	}

	@Test
	void create() {
		final var orders = List.of(new OrderResponse("buyerId",
				List.of(new OrderItemResponse(new OrderItemDetailResponse("image", "name", BigDecimal.TEN), 1))));
		assertNotNull(processor.create(orders));
	}

	@Test
	void expire() {
		processor.expire("cs_test_expire");
		// verify call in stripe-mock
	}

	@Test
	void handleEventAndGetUpdate_checkoutSessionCompleted() throws StripeException {
		final var session = Session.retrieve("cs_test_handleEventAndGetUpdate_checkoutSessionCompleted");
		session.setMetadata(metadata);
		session.setStatus(SessionListParams.Status.COMPLETE.getValue());

		try (final var webhook = mockStatic(Webhook.class)) {
			webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString())).thenReturn(event);
			when(event.getType()).thenReturn("checkout.session.completed");
			when(event.getDataObjectDeserializer()).thenReturn(deserializer);
			when(deserializer.getObject()).thenReturn(object);
			when(object.map(ArgumentMatchers.<Function<StripeObject, Session>>any())).thenReturn(Optional.of(session));

			final var update = processor.getEvents("payload", headers).map(PaymentEvents::updatePaymentStatus).get();
			assertEquals(buyerId, update.buyerId());
			assertEquals(SessionStatus.COMPLETE, update.status());
		}
	}

	@Test
	void handleEventAndGetUpdate_checkoutSessionExpired() throws StripeException {
		final var session = Session.retrieve("cs_test_handleEventAndGetUpdate_checkoutSessionExpired");
		session.setMetadata(metadata);
		session.setStatus(SessionListParams.Status.EXPIRED.getValue());

		try (final var webhook = mockStatic(Webhook.class)) {
			webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString())).thenReturn(event);
			when(event.getType()).thenReturn("checkout.session.expired");
			when(event.getDataObjectDeserializer()).thenReturn(deserializer);
			when(deserializer.getObject()).thenReturn(object);
			when(object.map(ArgumentMatchers.<Function<StripeObject, Session>>any())).thenReturn(Optional.of(session));

			final var events = processor.getEvents("payload", headers).get();
			assertEquals(SessionStatus.EXPIRED, events.updatePaymentStatus().status());
			assertEquals(Status.INACTIVE, events.updateOrderStatus().status());
		}
	}

	@Test
	void handleEventAndGetUpdate_paymentIntentSucceeded() throws StripeException {
		final var paymentIntent = PaymentIntent.retrieve("pi_handleEventAndGetUpdate_paymentIntentSucceeded");
		paymentIntent.setMetadata(metadata);
		paymentIntent.setStatus("succeeded");

		try (final var webhook = mockStatic(Webhook.class)) {
			webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString())).thenReturn(event);
			when(event.getType()).thenReturn("payment_intent.succeeded");
			when(event.getDataObjectDeserializer()).thenReturn(deserializer);
			when(deserializer.getObject()).thenReturn(object);
			when(object.map(ArgumentMatchers.<Function<StripeObject, PaymentIntent>>any()))
				.thenReturn(Optional.of(paymentIntent));

			final var events = processor.getEvents("payload", headers).get();
			assertEquals(buyerId, events.updateCartOrdered().buyerId());
			assertEquals(Status.CONFIRMED, events.updateOrderStatus().status());
		}
	}

	@Test
	void handleEventAndGetUpdate_paymentIntentPaymentFailed() throws StripeException {
		final var paymentIntent = PaymentIntent.retrieve("pi_handleEventAndGetUpdate_paymentIntentPaymentFailed");
		paymentIntent.setMetadata(metadata);

		try (final var webhook = mockStatic(Webhook.class)) {
			webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString())).thenReturn(event);
			when(event.getType()).thenReturn("payment_intent.payment_failed");
			when(event.getDataObjectDeserializer()).thenReturn(deserializer);
			when(deserializer.getObject()).thenReturn(object);
			when(object.map(ArgumentMatchers.<Function<StripeObject, PaymentIntent>>any()))
				.thenReturn(Optional.of(paymentIntent));

			final var update = processor.getEvents("payload", headers).map(PaymentEvents::updateOrderStatus).get();
			assertEquals(Status.INACTIVE, update.status());
		}
	}

}
