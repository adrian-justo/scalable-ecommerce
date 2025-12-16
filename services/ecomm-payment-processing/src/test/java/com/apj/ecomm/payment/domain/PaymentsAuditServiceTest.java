package com.apj.ecomm.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.payment.domain.model.Paged;
import com.apj.ecomm.payment.web.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentsAuditServiceTest {

	private List<Payment> payments;

	@Mock
	private PaymentRepository repository;

	@Spy
	private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

	@InjectMocks
	private PaymentsAuditService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/payments.json")) {
			payments = objMap.readValue(inputStream, new TypeReference<List<Payment>>() {
			});
		}
	}

	@Test
	void findAll() {
		final var response = payments.stream().map(mapper::toAudit).toList();
		when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(payments));
		assertEquals(new Paged<>(new PageImpl<>(response)), service.findAll(PageRequest.ofSize(10)));
	}

	@Test
	void findById_found() {
		final var payment = payments.getFirst();
		when(repository.findById(anyLong())).thenReturn(Optional.of(payment));
		assertEquals(mapper.toAudit(payment), service.findById(1L));
	}

	@Test
	void findById_notFound() {
		when(repository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> service.findById(9));
	}

}
