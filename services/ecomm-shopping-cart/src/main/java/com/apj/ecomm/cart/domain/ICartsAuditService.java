package com.apj.ecomm.cart.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;

public interface ICartsAuditService {

	Paged<CartResponse> findAll(Pageable pageable);

	CartResponse findById(long id);

}
