package com.apj.ecomm.cart.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@Observed(name = "service.admin.cart")
@RequiredArgsConstructor
class CartsAuditService implements ICartsAuditService {

	private final CartRepository repository;

	private final CartMapper mapper;

	public Paged<CartResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toResponse));
	}

	public CartResponse findById(final long id) {
		return repository.findById(id).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

}
