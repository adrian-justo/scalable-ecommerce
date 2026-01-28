package com.apj.ecomm.order.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.order.domain.model.DeliveryInformationRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.client.OrderClient;
import com.apj.ecomm.order.web.exception.OrderStillProcessingException;
import com.apj.ecomm.order.web.exception.ResourceNotFoundException;
import com.apj.ecomm.order.web.messaging.account.NotificationType;
import com.apj.ecomm.order.web.messaging.account.RequestAccountInformationEvent;
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.cart.UpdateCartItemsEvent;
import com.apj.ecomm.order.web.messaging.notification.NotificationRequest;
import com.apj.ecomm.order.web.messaging.notification.Role;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;
import com.apj.ecomm.order.web.messaging.product.ReturnProductStockEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.order")
@RequiredArgsConstructor
class OrderService extends BaseService implements IOrderService {

	private final OrderRepository repository;

	private final OrderClient client;

	private final OrderMapper mapper;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public Paged<OrderResponse> findAllBy(final String buyerId, final Pageable pageable) {
		return getPaged(repository.findAllByBuyerIdAndStatusNot(buyerId, Status.INACTIVE.toString(), pageable),
				mapper::toResponse);
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(final long id, final String buyerId) {
		return getResponse(repository.findById(id), order -> buyerId.equals(order.getBuyerId()), mapper::toResponse);
	}

	public List<OrderResponse> checkOut(final String buyerId, final DeliveryInformationRequest request) {
		if (repository.existsByBuyerIdAndStatus(buyerId, Status.PROCESSING.toString()))
			throw new OrderStillProcessingException();

		final var items = client.getAllCartItems(buyerId);
		if (items.isEmpty())
			throw new ResourceNotFoundException("Cart item");

		final var existingOrders = repository.findAllByBuyerIdAndStatusIn(buyerId,
				List.of(Status.ACTIVE.toString(), Status.INACTIVE.toString()));

		final var itemsForReturn = new ArrayList<OrderItem>();
		final var existingOrdersMap = mapByShopId(existingOrders, itemsForReturn);

		final var entities = mapper.toEntities(buyerId, request, items, existingOrdersMap);
		// Save order leaving details blank. It will be populated asynchronously.
		final var response = saveAndPublish(entities, buyerId);
		if (!itemsForReturn.isEmpty()) {
			eventPublisher.publishEvent(new ReturnProductStockEvent(toIdQuantityMap(itemsForReturn)));
		}
		return response;
	}

	private Map<String, Order> mapByShopId(final List<Order> existingOrders, final List<OrderItem> itemsForReturn) {
		final var existingOrdersMap = new HashMap<String, Order>();
		existingOrders.forEach(order -> {
			existingOrdersMap.put(order.getShopId(), order);
			if (Status.ACTIVE.toString().equals(order.getStatus())) {
				itemsForReturn.addAll(order.getProducts());
			}
		});
		return existingOrdersMap;
	}

	private List<OrderResponse> saveAndPublish(final List<Order> entities, final String buyerId) {
		final var response = new ArrayList<OrderResponse>();
		final var userIds = new HashSet<String>();

		final var saved = repository.saveAll(entities);
		saved.forEach(order -> {
			response.add(mapper.toResponse(order));
			userIds.add(order.getShopId());
		});

		eventPublisher.publishEvent(new RequestAccountInformationEvent(buyerId, userIds));
		return response;
	}

	public Optional<Map<Long, Integer>> updateInformationAndGetProducts(final String buyerId,
			final Map<String, UserResponse> userInformation) {
		final var itemsForCartUpdate = new ArrayList<OrderItem>();
		final var itemsForProductUpdate = new ArrayList<OrderItem>();

		final var entities = repository.findAllByBuyerIdAndStatus(buyerId, Status.PROCESSING.toString());
		entities
			.forEach(order -> updateOrDeactivate(order, userInformation, itemsForProductUpdate, itemsForCartUpdate));

		repository.saveAll(entities);
		if (!itemsForCartUpdate.isEmpty()) {
			publishUpdateCartItemsEvent(buyerId, toIdQuantityMap(itemsForCartUpdate));
		}
		return itemsForProductUpdate.isEmpty() ? Optional.empty() : Optional.of(toIdQuantityMap(itemsForProductUpdate));
	}

	private void updateOrDeactivate(final Order order, final Map<String, UserResponse> userInformation,
			final List<OrderItem> itemsForProductUpdate, final List<OrderItem> itemsForCartUpdate) {
		if (userInformation.containsKey(order.getBuyerId()) && userInformation.containsKey(order.getShopId())) {
			mapper.updateInfo(order, userInformation);
			itemsForProductUpdate.addAll(order.getProducts());
		}
		else {
			updateStatus(order, Status.INACTIVE);
			itemsForCartUpdate.addAll(order.getProducts());
		}
	}

	public Optional<List<OrderResponse>> populateDetailAndGetOrders(final String buyerId,
			final Map<Long, ProductResponse> details) {
		final var itemsForCartUpdate = new ArrayList<OrderItem>();
		final var itemsForRemoval = new ArrayList<OrderItem>();
		final var entities = repository.findAllByBuyerIdAndStatus(buyerId, Status.PROCESSING.toString());

		entities.stream()
			.map(Order::getProducts)
			.flatMap(List::stream)
			.forEach(item -> populateFrom(details.get(item.getProductId()), item, itemsForCartUpdate, itemsForRemoval));

		final var forRemoval = itemsForRemoval.stream().collect(Collectors.groupingBy(OrderItem::getOrder));
		final var forCheckout = new ArrayList<OrderResponse>();
		entities.forEach(order -> updateOrDeactivate(order, forRemoval.get(order), forCheckout));

		repository.saveAll(entities);
		if (!itemsForCartUpdate.isEmpty()) {
			publishUpdateCartItemsEvent(buyerId, toIdQuantityMap(itemsForCartUpdate));
		}
		return forCheckout.isEmpty() ? Optional.empty() : Optional.of(forCheckout);
	}

	private void populateFrom(final ProductResponse detail, final OrderItem item,
			final List<OrderItem> itemsForCartUpdate, final List<OrderItem> itemsForRemoval) {
		final var stock = detail.stock();
		if (stock < item.getQuantity()) {
			item.setQuantity(stock);
			itemsForCartUpdate.add(item);
			if (stock < 1) {
				itemsForRemoval.add(item);
			}
		}
		item.setProductDetail(mapper.toEntity(detail));
	}

	private void updateOrDeactivate(final Order order, final List<OrderItem> items,
			final List<OrderResponse> forCheckout) {
		final var products = order.getProducts();
		if (items != null) {
			products.removeAll(items);
		}

		if (!products.isEmpty()) {
			updateStatus(order, Status.ACTIVE);
			order.computeTotals();
			forCheckout.add(mapper.toResponse(order));
		}
		else {
			updateStatus(order, Status.INACTIVE);
		}
	}

	private void publishUpdateCartItemsEvent(final String buyerId, final Map<Long, Integer> products) {
		eventPublisher.publishEvent(new UpdateCartItemsEvent(buyerId, products));
	}

	private Map<Long, Integer> toIdQuantityMap(final List<OrderItem> items) {
		return items.stream().collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
	}

	public Optional<Map<String, BigDecimal>> updateStatusAndGetDetails(final String buyerId, final Status status) {
		final var entities = repository.findAllByBuyerIdAndStatus(buyerId, Status.ACTIVE.toString());
		entities.stream().forEach(order -> updateStatus(order, status));
		final var updated = repository.saveAll(entities);
		publishNotificationRequest(updated, status);
		return status == Status.CONFIRMED
				? Optional.of(updated.stream().collect(Collectors.toMap(Order::getShopId, Order::getTotal)))
				: Optional.empty();
	}

	private void updateStatus(final Order order, final Status status) {
		order.setStatus(mapper.valueOf(status));
	}

	private void publishNotificationRequest(final List<Order> orders, final Status status) {
		orders.stream().flatMap(order -> getNotificationRequests(order, status)).forEach(eventPublisher::publishEvent);
	}

	private Stream<NotificationRequest> getNotificationRequests(final Order order, final Status status) {
		final var deliveryInfo = order.getDeliveryInformation();
		final var shopInfo = order.getShopInformation();
		return Stream.concat(
				deliveryInfo.getNotificationTypes()
					.stream()
					.map(type -> getNotificationRequest(order, Role.BUYER, deliveryInfo.getRecipientBy(type), type,
							status)),
				shopInfo.getNotificationTypes()
					.stream()
					.map(type -> getNotificationRequest(order, Role.SELLER, shopInfo.getRecipientBy(type), type,
							status)));
	}

	private NotificationRequest getNotificationRequest(final Order order, final Role role, final String recipient,
			final NotificationType type, final Status status) {
		return new NotificationRequest(order.getId(), order.getBuyerId(), role, recipient, type, status);
	}

}