package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.client.product.ProductResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface CartItemMapper {

	default List<CartItemDetail> toDetails(final List<CartItem> items,
			@Context final Stream<ProductResponse> products) {
		final var map = products.collect(Collectors.toMap(ProductResponse::id, Function.identity()));
		return items.stream()
			.filter(item -> map.get(item.getProductId()) != null)
			.map(item -> toDetail(item, map.get(item.getProductId())))
			.toList();
	}

	CartItemDetail toDetail(CartItem item, ProductResponse product);

	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	CartItemResponse toResponse(CartItem item);

	@Named("toFullResponse")
	@Mapping(target = "id", ignore = true)
	CartItemResponse toFullResponse(CartItem item);

	default List<CartItem> toAddItems(final List<CartItemRequest> requestList, final List<CartItem> existingItems,
			final Map<Long, ProductResponse> validProducts, final Cart cart) {
		final var existing = existingItems.stream()
			.collect(Collectors.toMap(CartItem::getProductId, Function.identity()));
		return requestList.stream()
			.filter(request -> validProducts.containsKey(request.productId()))
			.map(request -> updateOrAdd(request, existing, cart))
			.map(item -> syncFrom(validProducts.get(item.getProductId()), item))
			.toList();
	}

	private CartItem updateOrAdd(final CartItemRequest request, final Map<Long, CartItem> existingItems,
			final Cart cart) {
		return Optional.ofNullable(existingItems.get(request.productId()))
			.map(existing -> updateAddQuantity(request, existing))
			.orElse(toEntity(cart, request));
	}

	@Mapping(target = "quantity", ignore = true)
	@BeanMapping(qualifiedByName = "addQuantity")
	CartItem updateAddQuantity(CartItemRequest updated, @MappingTarget CartItem existing);

	@Named("addQuantity")
	@AfterMapping
	default void addQuantity(final CartItemRequest updated, @MappingTarget final CartItem existing) {
		existing.setQuantity(existing.getQuantity() + Optional.ofNullable(updated.quantity()).orElse(1));
	}

	@Mapping(target = "quantity", defaultValue = "1")
	@Mapping(target = "cart", source = "cart")
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	CartItem toEntity(Cart cart, CartItemRequest request);

	default List<CartItem> toUpdateItems(final List<CartItem> existingItems, final List<CartItemRequest> requestList,
			final Map<Long, ProductResponse> validProducts) {
		return toUpdateItemsStream(existingItems, requestList)
			.map(item -> syncFrom(validProducts.get(item.getProductId()), item))
			.toList();
	}

	default List<CartItem> toUpdateItems(final List<CartItem> existingItems, final List<CartItemRequest> requestList) {
		return toUpdateItemsStream(existingItems, requestList).toList();
	}

	private Stream<CartItem> toUpdateItemsStream(final List<CartItem> existingItems,
			final List<CartItemRequest> requestList) {
		final var map = requestList.stream().collect(Collectors.toMap(CartItemRequest::productId, Function.identity()));
		return existingItems.stream()
			.filter(existing -> map.containsKey(existing.getProductId()))
			.map(existing -> updateEntity(map.get(existing.getProductId()), existing));
	}

	CartItem updateEntity(CartItemRequest updated, @MappingTarget CartItem existing);

	private CartItem syncFrom(final ProductResponse product, final CartItem item) {
		if (product == null) {
			item.setQuantity(0);
			return item;
		}
		if (item.getQuantity() > product.stock()) {
			item.setQuantity(product.stock());
		}
		if (StringUtils.isBlank(item.getShopId())) {
			item.setShopId(product.shopId());
		}
		return item;
	}

}