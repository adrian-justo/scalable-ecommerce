package com.apj.ecomm.payment.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.payment.domain.model.Paged;
import com.apj.ecomm.payment.domain.model.PaymentResponse;
import com.apj.ecomm.payment.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@Observed(name = "service.admin.payment")
@RequiredArgsConstructor
class PaymentsAuditService implements IPaymentsAuditService {

	private final PaymentRepository repository;

	private final PaymentMapper mapper;

	@Override
	public Paged<PaymentResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toAudit));
	}

	@Override
	public PaymentResponse findById(final long id) {
		return repository.findById(id).map(mapper::toAudit).orElseThrow(ResourceNotFoundException::new);
	}

}
