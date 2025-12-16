package com.apj.ecomm.payment.domain;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.payment.domain.model.PaymentEvents;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Observed(name = "service.webhook.payment")
@RequiredArgsConstructor
@Slf4j
class PaymentWebhookService implements IPaymentWebhookService {

	private final IPaymentService paymentService;

	private final PaymentProcessor processor;

	private final ApplicationEventPublisher eventPublisher;

	public void handleEvent(final String payload, final HttpHeaders headers) {
		processor.getEvents(payload, headers).ifPresent(this::handle);
	}

	private void handle(final PaymentEvents events) {
		Optional.ofNullable(events.updatePaymentStatus())
			.ifPresent(event -> paymentService.findOpenSession(event.buyerId())
				.ifPresent(payment -> paymentService.update(payment, event.status())));
		Optional.ofNullable(events.updateCartOrdered()).ifPresent(eventPublisher::publishEvent);
		Optional.ofNullable(events.updateOrderStatus()).ifPresent(eventPublisher::publishEvent);
	}

}
