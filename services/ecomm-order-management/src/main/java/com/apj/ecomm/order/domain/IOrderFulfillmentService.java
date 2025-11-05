package com.apj.ecomm.order.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.order.domain.model.CompleteOrderRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;

public interface IOrderFulfillmentService {

	boolean activeOrderExists(String shopId);

	Paged<OrderResponse> findAllBy(String shopId, Pageable pageable);

	OrderResponse findById(long id, String shopId);

	OrderResponse update(long id, String shopId, CompleteOrderRequest request);

}