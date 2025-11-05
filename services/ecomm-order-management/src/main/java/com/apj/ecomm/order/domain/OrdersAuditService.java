package com.apj.ecomm.order.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@Observed(name = "service.admin.order")
@RequiredArgsConstructor
class OrdersAuditService extends BaseService implements IOrdersAuditService {

	private final OrderRepository repository;

	private final OrderMapper mapper;

	public Paged<OrderResponse> findAll(final Pageable pageable) {
		return getPaged(repository.findAll(pageable), mapper::toAudit);
	}

	public OrderResponse findById(final long id) {
		return getResponse(repository.findById(id), mapper::toAudit);
	}

}
