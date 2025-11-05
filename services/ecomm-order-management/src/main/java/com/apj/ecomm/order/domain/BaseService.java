package com.apj.ecomm.order.domain;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.data.domain.Page;

import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;

class BaseService {

	Paged<OrderResponse> getPaged(final Page<Order> result, final Function<Order, OrderResponse> mapper) {
		return new Paged<>(result.map(mapper));
	}

	OrderResponse getResponse(final Optional<Order> result, final Function<Order, OrderResponse> mapper) {
		return result.map(mapper).orElseThrow(ResourceNotFoundException::new);
	}

	OrderResponse getResponse(final Optional<Order> result, final Predicate<Order> predicate,
			final Function<Order, OrderResponse> mapper) {
		return filter(result, predicate).map(mapper).orElseThrow(ResourceAccessDeniedException::new);
	}

	Order getEntity(final Optional<Order> result, final Predicate<Order> predicate) {
		return filter(result, predicate).orElseThrow(ResourceAccessDeniedException::new);
	}

	private Optional<Order> filter(final Optional<Order> result, final Predicate<Order> predicate) {
		if (result.isEmpty())
			throw new ResourceNotFoundException();
		return result.filter(predicate);
	}

}
