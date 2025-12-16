package com.apj.ecomm.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.apj.ecomm.payment.web.exception.ResourceNotFoundException;
import com.apj.ecomm.payment.web.messaging.order.OrderItemDetailResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderItemResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	private Payment payment;

	private String buyerId;

	@Mock
	private PaymentRepository repository;

	@Mock
	private PaymentProcessor processor;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Spy
	private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

	@InjectMocks
	private PaymentService service;

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
	void getSession_found() {
		when(processor.getValue(any(SessionStatus.class))).thenReturn(SessionStatus.OPEN.toString());
		when(repository.findByBuyerIdAndStatus(anyString(), anyString())).thenReturn(Optional.of(payment));
		assertEquals(payment.getSessionUrl(), service.getSession(buyerId));
	}

	@Test
	void getSession_notFound() {
		when(processor.getValue(any(SessionStatus.class))).thenReturn(SessionStatus.OPEN.toString());
		when(repository.findByBuyerIdAndStatus(anyString(), anyString())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.getSession(buyerId));
	}

	@Test
	void createSession() {
		final var orders = List.of(new OrderResponse(buyerId,
				List.of(new OrderItemResponse(new OrderItemDetailResponse("image", "name", BigDecimal.TEN), 1))));

		when(processor.getValue(any(SessionStatus.class))).thenReturn(SessionStatus.OPEN.toString());
		when(repository.findByBuyerIdAndStatus(anyString(), anyString())).thenReturn(Optional.empty());
		when(processor.create(ArgumentMatchers.<List<OrderResponse>>any())).thenReturn(payment);
		when(repository.save(any(Payment.class))).thenReturn(payment);

		assertEquals(mapper.toResponse(payment), service.createSession(orders));
	}

	@Test
	void createSession_hasOpen() {
		final var orders = List.of(new OrderResponse(buyerId,
				List.of(new OrderItemResponse(new OrderItemDetailResponse("image", "name", BigDecimal.TEN), 1))));
		final var openSession = new Payment();
		openSession.setSessionId(payment.getSessionId());

		when(processor.getValue(any(SessionStatus.class))).thenReturn(SessionStatus.OPEN.toString());
		when(repository.findByBuyerIdAndStatus(anyString(), anyString())).thenReturn(Optional.of(openSession));
		doNothing().when(processor).expire(anyString());
		when(processor.create(ArgumentMatchers.<List<OrderResponse>>any())).thenReturn(payment);
		when(repository.save(any(Payment.class))).thenReturn(payment);

		assertEquals(mapper.toResponse(payment), service.createSession(orders));
		verify(processor, times(1)).expire(payment.getSessionId());
	}

}
