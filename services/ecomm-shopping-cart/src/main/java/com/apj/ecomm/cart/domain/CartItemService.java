package com.apj.ecomm.cart.domain;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.cart.domain.model.CartItemCatalog;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.exception.ResourceAccessDeniedException;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.cart.product")
@RequiredArgsConstructor
class CartItemService implements ICartItemService {

	private final CartRepository cartRepository;

	private final CartItemRepository repository;

	private final CartClient client;

	private final CartItemMapper mapper;

	@Transactional(readOnly = true)
	public List<CartItemCatalog> findAll(final long cartId, final String buyerId) {
		validate(cartId, buyerId);
		final var items = repository.findAllByCartId(cartId);
		return mapper.toCatalog(items, getProductsFrom(client, items));
	}

	@Transactional(readOnly = true)
	public CartItemDetail findById(final long cartId, final String buyerId, final long productId) {
		validate(cartId, buyerId, productId);
		return repository.findById(new CartItemId(cartId, productId))
			.map(item -> mapper.toDetail(item, client.getProductById(productId)))
			.orElseThrow(ResourceNotFoundException::new);
	}

	public List<CartItemResponse> addAll(final long cartId, final String buyerId,
			final List<CartItemRequest> requestList) {
		final var cart = validate(cartId, buyerId);
		final var items = mapper.toEntities(requestList, repository.findAllByCartId(cartId),
				getProductsBy(requestList.stream().map(CartItemRequest::productId), client), cart);
		return repository.saveAll(items).stream().map(mapper::toResponse).toList();
	}

	public List<CartItemResponse> updateAll(final long cartId, final String buyerId,
			final List<CartItemRequest> requestList) {
		validate(cartId, buyerId);
		final var items = mapper.toEntities(repository.findAllByCartId(cartId), requestList);
		return repository.saveAll(items).stream().map(mapper::toResponse).toList();
	}

	public void deleteAll(final long cartId, final String buyerId, final List<Long> productIds) {
		final var cart = validate(cartId, buyerId);
		final var items = repository.findAllByCartId(cartId)
			.stream()
			.filter(item -> productIds.stream().anyMatch(id -> id.equals(item.getProductId())))
			.toList();

		cart.getProducts().removeAll(items);
		cartRepository.save(cart);
		repository.deleteAll(items);
	}

	private Cart validate(final long cartId, final String buyerId) {
		return cartRepository.findById(cartId)
			.filter(c -> c.getBuyerId().equals(buyerId) && !c.isOrdered())
			.orElseThrow(ResourceAccessDeniedException::new);
	}

	private Cart validate(final long cartId, final String buyerId, final long productId) {
		final var cart = validate(cartId, buyerId);
		if (cart.getProducts().stream().noneMatch(p -> p.getProductId().equals(productId)))
			throw new ResourceAccessDeniedException("product");
		return cart;
	}

}
