package com.apj.ecomm.order.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.order.domain.model.CompleteOrderRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.shop.order")
@RequiredArgsConstructor
class OrderFulfillmentService extends BaseService implements IOrderFulfillmentService {

	private final OrderRepository repository;

	private final OrderMapper mapper;

	@Transactional(readOnly = true)
	public boolean activeOrderExists(final String shopId) {
		return repository.existsByShopIdAndStatusIn(shopId,
				List.of(Status.PROCESSING.toString(), Status.ACTIVE.toString(), Status.CONFIRMED.toString()));
	}

	@Transactional(readOnly = true)
	public Paged<OrderResponse> findAllBy(final String shopId, final Pageable pageable) {
		return getPaged(repository.findAllByShopIdAndStatusNot(shopId, Status.INACTIVE.toString(), pageable),
				mapper::toResponse);
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(final long id, final String shopId) {
		return getResponse(repository.findById(id), order -> shopId.equals(order.getShopId()), mapper::toResponse);
	}

	public OrderResponse update(final long id, final String shopId, final CompleteOrderRequest request) {
		return mapper.toResponse(repository.save(mapper.updateEntity(request, Status.COMPLETED, getEntity(
				repository.findById(id),
				order -> shopId.equals(order.getShopId()) && Status.CONFIRMED.toString().equals(order.getStatus())))));
	}

}
