package com.apj.ecomm.payment.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.payment.domain.model.Paged;
import com.apj.ecomm.payment.domain.model.PaymentResponse;

public interface IPaymentsAuditService {

	Paged<PaymentResponse> findAll(Pageable pageable);

	PaymentResponse findById(long id);

}
