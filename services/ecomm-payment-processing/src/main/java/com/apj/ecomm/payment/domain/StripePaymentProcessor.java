package com.apj.ecomm.payment.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.domain.model.PaymentEvents;
import com.apj.ecomm.payment.domain.model.UpdatePaymentStatus;
import com.apj.ecomm.payment.web.messaging.cart.UpdateCartOrderedEvent;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;
import com.apj.ecomm.payment.web.messaging.order.Status;
import com.apj.ecomm.payment.web.messaging.order.UpdateOrderStatusEvent;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.PaymentIntentData;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Observed(name = "processor.payment")
@RequiredArgsConstructor
@Slf4j
class StripePaymentProcessor implements PaymentProcessor {

	@Value("${stripe.key}")
	private String stripeKey;

	@Value("${success.url}")
	private String successUrl;

	@Value("${stripe.webhook.secret}")
	private String webhookSecret;

	private final StripeMapper mapper;

	@PostConstruct
	void setKey() {
		Stripe.apiKey = stripeKey;
	}

	public String getValue(final SessionStatus status) {
		return mapper.toStripe(status).getValue();
	}

	public Payment create(final List<OrderResponse> orders) {
		final var buyerId = orders.getFirst().buyerId();
		final var params = SessionCreateParams.builder()
			.setMode(Mode.PAYMENT)
			.putMetadata(AppConstants.HEADER_USER_ID, buyerId)
			.addAllLineItem(
					orders.stream().map(OrderResponse::products).flatMap(List::stream).map(mapper::toLineItem).toList())
			.setSuccessUrl(successUrl)
			.setPaymentIntentData(PaymentIntentData.builder()
				.putMetadata(AppConstants.HEADER_USER_ID, buyerId)
				.setTransferGroup(UUID.randomUUID().toString())
				.build())
			.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)).getEpochSecond())
			.build();

		Session session = null;
		try {
			session = Session.create(params);
		}
		catch (final StripeException e) {
			handle(e);
		}

		return mapper.toEntity(session, buyerId);
	}

	public void expire(final String id) {
		try {
			Session.retrieve(id).expire();
		}
		catch (final StripeException e) {
			handle(e);
		}
	}

	public Optional<PaymentEvents> getEvents(final String payload, final HttpHeaders headers) {
		try {
			final var event = Webhook.constructEvent(payload, headers.getFirst("Stripe-Signature"), webhookSecret);
			return switch (event.getType()) {
				case "checkout.session.completed" -> fromSession(event);
				case "checkout.session.expired" -> fromSession(event);
				case "payment_intent.succeeded" -> fromPaymentIntent(event);
				case "payment_intent.payment_failed" -> fromPaymentIntent(event);
				default -> {
					log.warn("Unhandled event type: " + event.getType());
					yield Optional.empty();
				}
			};
		}
		catch (final StripeException e) {
			handle(e);
			return Optional.empty();
		}
	}

	private Optional<PaymentEvents> fromSession(final Event event) {
		return getObject(event).map(Session.class::cast).map(this::getPaymentEvents);
	}

	private PaymentEvents getPaymentEvents(final Session session) {
		final var buyerId = session.getMetadata().get(AppConstants.HEADER_USER_ID);
		final var status = mapper.fromStripe(session.getStatus().toUpperCase());
		return new PaymentEvents(new UpdatePaymentStatus(buyerId, status),
				SessionStatus.EXPIRED.equals(status) ? updateOrderInactive(buyerId) : null);
	}

	private Optional<PaymentEvents> fromPaymentIntent(final Event event) {
		return getObject(event).map(PaymentIntent.class::cast).map(this::getPaymentEvents);
	}

	private PaymentEvents getPaymentEvents(final PaymentIntent paymentIntent) {
		final var buyerId = paymentIntent.getMetadata().get(AppConstants.HEADER_USER_ID);
		final var successfulPayment = "succeeded".equals(paymentIntent.getStatus());
		return new PaymentEvents(
				successfulPayment ? new UpdateOrderStatusEvent(buyerId, Status.CONFIRMED, paymentIntent.getId())
						: updateOrderInactive(buyerId),
				successfulPayment ? new UpdateCartOrderedEvent(buyerId) : null);
	}

	private Optional<StripeObject> getObject(final Event event) {
		return event.getDataObjectDeserializer().getObject();
	}

	private UpdateOrderStatusEvent updateOrderInactive(final String buyerId) {
		return new UpdateOrderStatusEvent(buyerId, Status.INACTIVE);
	}

	private void handle(final StripeException e) {
		e.printStackTrace();
	}

}
