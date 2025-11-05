package com.apj.ecomm.order.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.order.domain.model.CompleteOrderRequest;
import com.apj.ecomm.order.domain.model.DeliveryInformationRequest;
import com.apj.ecomm.order.domain.model.InformationResponse;
import com.apj.ecomm.order.domain.model.OrderItemDetailResponse;
import com.apj.ecomm.order.domain.model.OrderItemResponse;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.web.client.cart.CartItemResponse;
import com.apj.ecomm.order.web.messaging.ProductResponse;
import com.apj.ecomm.order.web.messaging.UserResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface OrderMapper {

	@Mapping(target = "deliveryInformation", ignore = true)
	@Mapping(target = "shopInformation", ignore = true)
	@Mapping(target = "total", ignore = true)
	@Mapping(target = "products", qualifiedByName = "toAudit")
	OrderResponse toAudit(Order entity);

	@Mapping(target = "products", qualifiedByName = "toResponse")
	OrderResponse toResponse(Order entity);

	InformationResponse toResponse(DeliveryInformation entity);

	InformationResponse toResponse(ShopInformation entity);

	@Named("toResponse")
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	OrderItemResponse toResponse(OrderItem entity);

	@Named("toAudit")
	@Mapping(target = "productDetail", ignore = true)
	@Mapping(target = "totalPrice", ignore = true)
	OrderItemResponse toAudit(OrderItem entity);

	OrderItemDetailResponse toResponse(OrderItemDetail entity);

	default List<Order> toEntities(final String buyerId, final DeliveryInformationRequest request,
			final List<CartItemResponse> items, final Map<String, Order> existingOrders) {
		return items.stream()
			.collect(Collectors.groupingBy(CartItemResponse::shopId))
			.entrySet()
			.stream()
			.map(entry -> updateOrAdd(buyerId, request, entry.getKey(), entry.getValue(), existingOrders))
			.toList();
	}

	private Order updateOrAdd(final String buyerId, final DeliveryInformationRequest request, final String shopId,
			final List<CartItemResponse> products, final Map<String, Order> existingOrders) {
		return Optional.ofNullable(existingOrders.get(shopId))
			.map(existing -> updateEntity(request, products, Status.PROCESSING, existing))
			.orElse(toEntity(buyerId, request, shopId, products, Status.PROCESSING));
	}

	Order toEntity(String buyerId, DeliveryInformationRequest deliveryInformation, String shopId,
			List<CartItemResponse> products, Status status);

	@Mapping(target = "products", ignore = true)
	@BeanMapping(qualifiedByName = "updateItems")
	Order updateEntity(DeliveryInformationRequest deliveryInformation, List<CartItemResponse> products, Status status,
			@MappingTarget Order existing);

	@BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	DeliveryInformation toEntity(DeliveryInformationRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	DeliveryInformation updateEntity(DeliveryInformationRequest updated, @MappingTarget DeliveryInformation existing);

	@Named("updateItems")
	@AfterMapping
	default void updateItems(final List<CartItemResponse> products, final Order order) {
		final var map = products.stream().collect(Collectors.toMap(CartItemResponse::productId, Function.identity()));
		order.getProducts()
			.removeAll(order.getProducts().stream().filter(item -> !map.containsKey(item.getProductId())).toList());
		updateOrAdd(map, order.getProducts());
		order.initializeProducts();
	}

	private void updateOrAdd(final Map<Long, CartItemResponse> map, final List<OrderItem> items) {
		items.forEach(existing -> updateEntity(map.get(existing.getProductId()), existing));

		final var itemsMap = items.stream().collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));
		items.addAll(map.values()
			.stream()
			.filter(response -> !itemsMap.containsKey(response.productId()))
			.map(this::toEntity)
			.toList());
	}

	@Mapping(target = "totalPrice", expression = "java(BigDecimal.ZERO)")
	OrderItem toEntity(CartItemResponse response);

	@Mapping(target = "productId", ignore = true)
	@Mapping(target = "productDetail", expression = "java(null)")
	void updateEntity(CartItemResponse response, @MappingTarget OrderItem existing);

	String valueOf(Status status);

	default void updateInfo(final Order order, final Map<String, UserResponse> userInformation) {
		order.setDeliveryInformation(
				updateEntity(order.getDeliveryInformation(), userInformation.get(order.getBuyerId())));
		order.setShopInformation(updateEntity(order.getShopInformation(), userInformation.get(order.getShopId())));
	}

	private DeliveryInformation updateEntity(final DeliveryInformation updated, final UserResponse response) {
		return updateEntity(updated, toDeliveryInfo(response));
	}

	DeliveryInformation toDeliveryInfo(UserResponse response);

	DeliveryInformation updateEntity(DeliveryInformation updated, @MappingTarget DeliveryInformation existing);

	private ShopInformation updateEntity(final ShopInformation updated, final UserResponse response) {
		return updateEntity(updated, toShopInfo(response));
	}

	@Mapping(target = "name", source = "shopName")
	ShopInformation toShopInfo(UserResponse response);

	ShopInformation updateEntity(ShopInformation updated, @MappingTarget ShopInformation existing);

	@Mapping(target = "image", source = "images", qualifiedByName = "getFirst")
	OrderItemDetail toEntity(ProductResponse response);

	@Named("getFirst")
	default String getFirst(final List<String> images) {
		return images.getFirst();
	}

	Order updateEntity(CompleteOrderRequest request, Status status, @MappingTarget Order existing);

}
