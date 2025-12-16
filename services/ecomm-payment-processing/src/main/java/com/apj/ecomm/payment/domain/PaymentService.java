package com.apj.ecomm.payment.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.payment.domain.model.PaymentResponse;
import com.apj.ecomm.payment.web.exception.ResourceNotFoundException;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;
import com.apj.ecomm.payment.web.messaging.order.Status;
import com.apj.ecomm.payment.web.messaging.order.UpdateOrderStatusEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.payment")
@RequiredArgsConstructor
class PaymentService implements IPaymentService {

	private final PaymentRepository repository;

	private final PaymentProcessor processor;

	private final ApplicationEventPublisher eventPublisher;

	private final PaymentMapper mapper;

	public String getSession(final String buyerId) {
		return findOpenSession(buyerId).map(Payment::getSessionUrl)
			.orElseThrow(() -> new ResourceNotFoundException("Payment session"));
	}

	public Optional<Payment> findOpenSession(final String buyerId) {
		return repository.findByBuyerIdAndStatus(buyerId, processor.getValue(SessionStatus.OPEN));
	}

	public PaymentResponse createSession(final List<OrderResponse> orders) {
		findOpenSession(orders.getFirst().buyerId()).ifPresent(this::expire);
		final var created = processor.create(orders);
		final var saved = created.getStatus() == null ? update(created, SessionStatus.EXPIRED)
				: repository.save(created);
		if (saved.getStatus().equals(processor.getValue(SessionStatus.EXPIRED))) {
			eventPublisher.publishEvent(new UpdateOrderStatusEvent(saved.getBuyerId(), Status.INACTIVE));
		}
		return mapper.toResponse(saved);
	}

	private void expire(final Payment payment) {
		processor.expire(payment.getSessionId());
		update(payment, SessionStatus.EXPIRED);
	}

	public Payment update(final Payment payment, final SessionStatus status) {
		payment.setStatus(processor.getValue(status));
		return repository.save(payment);
	}

}
