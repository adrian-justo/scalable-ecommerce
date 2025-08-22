package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.Optional;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.cart.domain.model.CartItemCatalog;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;
import com.apj.ecomm.cart.web.client.product.ProductResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface CartItemMapper {

	List<CartItemCatalog> toCatalog(List<CartItem> items, @Context List<ProductCatalog> products);

	default CartItemCatalog toCatalog(final CartItem item, @Context final List<ProductCatalog> products) {
		return products.stream()
			.filter(product -> product.id().equals(item.getProductId()))
			.findFirst()
			.map(product -> toCatalog(item, product))
			.orElse(toCatalog(item, new ProductCatalog(item.getProductId(), null, null, null)));
	}

	CartItemCatalog toCatalog(CartItem item, ProductCatalog product);

	CartItemDetail toDetail(CartItem item, ProductResponse product);

	CartItemResponse toResponse(CartItem item);

	default List<CartItem> toEntities(final List<CartItemRequest> requestList, final List<CartItem> existingItems,
			final List<ProductCatalog> validProducts, final Cart cart) {
		return requestList.stream()
			.filter(request -> validProducts.stream().anyMatch(catalog -> catalog.id().equals(request.productId())))
			.map(request -> updateEntity(request, existingItems).orElse(toEntity(cart, request)))
			.toList();
	}

	default Optional<CartItem> updateEntity(final CartItemRequest updated, final List<CartItem> existingItems) {
		return existingItems.stream()
			.filter(existing -> existing.getProductId().equals(updated.productId()))
			.findFirst()
			.map(existing -> updateEntity(updated, existing));
	}

	@Mapping(target = "quantity", ignore = true)
	@BeanMapping(qualifiedByName = "addQuantity")
	CartItem updateEntity(CartItemRequest updated, @MappingTarget CartItem existing);

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

	default List<CartItem> toEntities(final List<CartItem> existingItems, final List<CartItemRequest> requestList) {
		return existingItems.stream()
			.filter(existing -> requestList.stream()
				.anyMatch(request -> request.productId().equals(existing.getProductId())))
			.map(existing -> updateEntity(existing, requestList))
			.toList();
	}

	default CartItem updateEntity(final CartItem existing, final List<CartItemRequest> requestList) {
		return requestList.stream()
			.filter(request -> request.productId().equals(existing.getProductId()))
			.findFirst()
			.map(request -> updateEntity(existing, request))
			.orElseThrow();
	}

	CartItem updateEntity(@MappingTarget CartItem existing, CartItemRequest updated);

}