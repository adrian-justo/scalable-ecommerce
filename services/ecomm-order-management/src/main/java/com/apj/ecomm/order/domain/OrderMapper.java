package com.apj.ecomm.order.domain;

import java.util.ArrayList;
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
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;

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
		final var itemsForRemoval = new ArrayList<OrderItem>();
		final var productMap = products.stream()
			.collect(Collectors.toMap(CartItemResponse::productId, Function.identity()));

		final var items = order.getProducts();
		items.forEach(item -> updateExisting(productMap, item, itemsForRemoval));
		items.removeAll(itemsForRemoval);
		items.addAll(productMap.values().stream().map(this::toEntity).toList());

		order.initializeProducts();
	}

	private void updateExisting(final Map<Long, CartItemResponse> productMap, final OrderItem item,
			final List<OrderItem> itemsForRemoval) {
		final var id = item.getProductId();
		if (productMap.containsKey(id)) {
			updateEntity(productMap.get(id), item);
			productMap.remove(id);
		}
		else {
			itemsForRemoval.add(item);
		}
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

	@Mapping(target = "notifEmail", source = "email")
	@Mapping(target = "notifMobileNo", source = "mobileNo")
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
