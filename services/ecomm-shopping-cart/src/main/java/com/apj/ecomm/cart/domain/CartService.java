package com.apj.ecomm.cart.domain;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.cart.domain.model.BuyerCartResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@Observed(name = "service.cart")
@RequiredArgsConstructor
class CartService implements ICartService {

	private final CartRepository repository;

	private final CartClient client;

	private final CartMapper mapper;

	public Paged<CartResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toResponse));
	}

	public CartResponse findById(final long id) {
		return repository.findById(id).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

	public BuyerCartResponse findByBuyerId(final String buyerId) {
		return getActiveCartBy(buyerId).map(cart -> mapper.toDetail(cart, getProductsFrom(client, cart.getProducts())))
			.orElseThrow(ResourceNotFoundException::new);
	}

	@Transactional
	public void createCart(final String buyerId) {
		if (getActiveCartBy(buyerId).isEmpty()) {
			repository.save(mapper.create(buyerId, List.of()));
		}
	}

	private Optional<Cart> getActiveCartBy(final String buyerId) {
		return repository.findAllByBuyerId(buyerId).stream().filter(not(Cart::isOrdered)).findFirst();
	}

}
