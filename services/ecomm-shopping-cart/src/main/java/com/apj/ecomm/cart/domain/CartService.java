package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.cart.domain.model.CartDetailResponse;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.cart.product")
@RequiredArgsConstructor
class CartService implements ICartService {

	private final CartRepository cartRepository;

	private final CartItemRepository repository;

	private final CartClient client;

	private final CartItemMapper mapper;

	private final CartMapper cartMapper;

	@Transactional(readOnly = true)
	public CartDetailResponse findCartBy(final String buyerId) {
		return findActiveCartBy(buyerId)
			.map(cart -> cartMapper.toDetail(cart, getDetails(cart.getProducts().stream().map(CartItem::getProductId))))
			.orElseThrow(ResourceNotFoundException::new);
	}

	public void createCart(final String buyerId) {
		if (findActiveCartBy(buyerId).isEmpty()) {
			cartRepository.save(create(buyerId));
		}
	}

	@Transactional(readOnly = true)
	public List<CartItemResponse> findItemsBy(final String buyerId) {
		final var cart = getActiveCartBy(buyerId);
		return cart.getProducts().stream().map(mapper::toResponse).toList();
	}

	private Stream<ProductResponse> getDetails(final Stream<Long> productIds) {
		final var ids = productIds.sorted().map(String::valueOf).collect(Collectors.joining(","));
		return ids.isEmpty() ? Stream.empty()
				: client.getAllProducts("stock>0;id:" + ids, Pageable.unpaged()).result().stream();
	}

	@Transactional(readOnly = true)
	public CartItemDetail findItemBy(final long productId, final String buyerId) {
		final var cart = getActiveCartBy(buyerId);
		final var items = cart.getProducts()
			.stream()
			.collect(Collectors.toMap(CartItem::getProductId, Function.identity()));
		if (!items.containsKey(productId))
			throw new ResourceNotFoundException("Item");
		return getDetails(items.keySet().stream()).filter(product -> product.id().equals(productId))
			.findFirst()
			.map(product -> mapper.toDetail(items.get(productId), product))
			.orElseThrow(() -> new ResourceNotFoundException("Item"));
	}

	public List<CartItemResponse> addItems(final String buyerId, final List<CartItemRequest> requestList) {
		final var cart = getActiveCartBy(buyerId);
		final var items = mapper.toAddItems(requestList, cart.getProducts(), getValidProducts(requestList, buyerId),
				cart);
		return saveOnlyValid(items, cart);
	}

	public List<CartItemResponse> updateItems(final String buyerId, final List<CartItemRequest> requestList) {
		final var cart = getActiveCartBy(buyerId);
		final var items = mapper.toUpdateItems(cart.getProducts(), requestList, getValidProducts(requestList, buyerId));
		return saveOnlyValid(items, cart);
	}

	private Map<Long, ProductResponse> getValidProducts(final List<CartItemRequest> requestList, final String buyerId) {
		return getDetails(requestList.stream().map(CartItemRequest::productId))
			.filter(product -> !buyerId.equals(product.shopId()))
			.collect(Collectors.toMap(ProductResponse::id, Function.identity()));
	}

	public List<CartItemResponse> updateItemsFromEvent(final String buyerId, final Map<Long, Integer> products) {
		final var cart = getActiveCartBy(buyerId);
		final var items = mapper.toUpdateItems(cart.getProducts(),
				products.entrySet()
					.stream()
					.map(entry -> new CartItemRequest(entry.getKey(), entry.getValue()))
					.toList());
		return saveOnlyValid(items, cart);
	}

	private List<CartItemResponse> saveOnlyValid(final List<CartItem> items, final Cart cart) {
		return repository.saveAll(removeOutOfStock(items, cart)).stream().map(mapper::toResponse).toList();
	}

	private List<CartItem> removeOutOfStock(final List<CartItem> entities, final Cart cart) {
		final var outOfStock = entities.stream().collect(Collectors.partitioningBy(item -> item.getQuantity() < 1));
		if (!outOfStock.get(true).isEmpty()) {
			deleteAll(outOfStock.get(true), cart);
		}
		return outOfStock.get(false);
	}

	public void deleteItems(final String buyerId, final List<Long> productIds) {
		final var cart = getActiveCartBy(buyerId);
		deleteAll(cart.getProducts().stream().filter(item -> productIds.contains(item.getProductId())).toList(), cart);
	}

	private void deleteAll(final List<CartItem> items, final Cart cart) {
		cart.getProducts().removeAll(items);
		cartRepository.save(cart);
	}

	public void updateCartOrdered(final String buyerId) {
		findActiveCartBy(buyerId).ifPresent(this::orderAndCreate);
	}

	private Cart getActiveCartBy(final String buyerId) {
		return findActiveCartBy(buyerId).orElseThrow(ResourceNotFoundException::new);
	}

	private Optional<Cart> findActiveCartBy(final String buyerId) {
		return cartRepository.findByBuyerIdAndActiveTrue(buyerId);
	}

	private void orderAndCreate(final Cart cart) {
		cart.setActive(false);
		cartRepository.saveAll(List.of(cart, create(cart.getBuyerId())));
	}

	private Cart create(final String buyerId) {
		return cartMapper.create(buyerId, List.of());
	}

}
