package com.apj.ecomm.order.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;

public interface IOrdersAuditService {

	Paged<OrderResponse> findAll(Pageable pageable);

	OrderResponse findById(long id);

}
