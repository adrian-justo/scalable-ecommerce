package com.apj.ecomm.payment.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;

import com.apj.ecomm.payment.domain.model.PaymentEvents;
import com.apj.ecomm.payment.domain.model.UpdatePaymentStatus;
import com.apj.ecomm.payment.web.messaging.cart.UpdateCartOrderedEvent;
import com.apj.ecomm.payment.web.messaging.order.Status;
import com.apj.ecomm.payment.web.messaging.order.UpdateOrderStatusEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

	private Payment payment;

	private String buyerId;

	@Mock
	private IPaymentService paymentService;

	@Mock
	private PaymentProcessor processor;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private PaymentWebhookService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/payments.json")) {
			final var payments = objMap.readValue(inputStream, new TypeReference<List<Payment>>() {
			});
			payment = payments.getFirst();
			buyerId = payment.getBuyerId();
		}
	}

	@Test
	void handleEvent_updatePaymentStatus() {
		final var events = new PaymentEvents(new UpdatePaymentStatus(buyerId, SessionStatus.COMPLETE), null);

		when(processor.getEvents(anyString(), any(HttpHeaders.class))).thenReturn(Optional.of(events));
		when(paymentService.findOpenSession(anyString())).thenReturn(Optional.of(payment));

		service.handleEvent(buyerId, new HttpHeaders());
		verify(paymentService, times(1)).update(payment, SessionStatus.COMPLETE);
	}

	@Test
	void handleEvent_publish() {
		final var events = new PaymentEvents(new UpdateOrderStatusEvent(buyerId, Status.CONFIRMED, "paymentIntentId"),
				new UpdateCartOrderedEvent(buyerId));

		when(processor.getEvents(anyString(), any(HttpHeaders.class))).thenReturn(Optional.of(events));

		service.handleEvent(buyerId, new HttpHeaders());
		final var inOrder = inOrder(eventPublisher);
		inOrder.verify(eventPublisher, times(1)).publishEvent(events.updateCartOrdered());
		inOrder.verify(eventPublisher, times(1)).publishEvent(events.updateOrderStatus());
	}

}
