package com.apj.ecomm.order.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import com.apj.ecomm.order.web.messaging.account.RequestAccountInformationEvent;
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.cart.UpdateCartItemsEvent;
import com.apj.ecomm.order.web.messaging.payment.CheckoutSessionRequest;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;
import com.apj.ecomm.order.web.messaging.product.ProductStockUpdate;
import com.apj.ecomm.order.web.messaging.product.ReturnProductStockEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.order")
@RequiredArgsConstructor
class OrderService extends BaseService implements IOrderService {

	private final OrderRepository repository;

	private final OrderItemRepository itemRepository;

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

		final var existingOrders = repository
			.findAllByBuyerIdAndStatusIn(buyerId, List.of(Status.ACTIVE.toString(), Status.INACTIVE.toString()))
			.stream()
			.collect(Collectors.groupingBy(Order::getStatus));

		final var items = client.getAllCartItems(buyerId);
		if (items.isEmpty())
			throw new ResourceNotFoundException("Cart item");

		final var entities = mapper.toEntities(buyerId, request, items, publishReturnAndGetAll(existingOrders));
		// Save order leaving details blank. It will be populated asynchronously.
		return publishCheckoutAndGetResponse(repository.saveAll(entities), buyerId);
	}

	private Map<String, Order> publishReturnAndGetAll(final Map<String, List<Order>> existingOrders) {
		final var existing = Optional.ofNullable(existingOrders.get(Status.ACTIVE.toString()))
			.orElse(new ArrayList<>());
		if (!existing.isEmpty()) {
			eventPublisher.publishEvent(new ReturnProductStockEvent(toIdQuantityMap(existing)));
		}

		Optional.ofNullable(existingOrders.get(Status.INACTIVE.toString())).ifPresent(existing::addAll);
		return existing.stream().collect(Collectors.toMap(Order::getShopId, Function.identity()));
	}

	private List<OrderResponse> publishCheckoutAndGetResponse(final List<Order> orders, final String buyerId) {
		final var userIds = orders.stream().map(Order::getShopId).collect(Collectors.toSet());
		userIds.add(buyerId);
		eventPublisher.publishEvent(new RequestAccountInformationEvent(buyerId, userIds));
		return orders.stream().map(mapper::toResponse).toList();
	}

	public ProductStockUpdate updateInformationAndGetStockUpdate(final String buyerId,
			final Map<String, UserResponse> userInformation) {
		final var validOrders = repository.findAllByBuyerIdAndStatus(buyerId, Status.PROCESSING.toString())
			.stream()
			.collect(Collectors.partitioningBy(order -> userInformation.containsKey(order.getBuyerId())
					&& userInformation.containsKey(order.getShopId())));

		final var orders = repository.saveAll(updateOrDeactivate(validOrders, userInformation));
		return orders.isEmpty() ? null : new ProductStockUpdate(buyerId, toIdQuantityMap(orders));
	}

	private List<Order> updateOrDeactivate(final Map<Boolean, List<Order>> validOrders,
			final Map<String, UserResponse> userInformation) {
		final var forUpdate = validOrders.get(true);
		forUpdate.forEach(order -> mapper.updateInfo(order, userInformation));

		final var forDeletion = validOrders.get(false);
		if (!forDeletion.isEmpty()) {
			publishUpdateCartItemsEvent(forDeletion.getFirst().getBuyerId(), toIdQuantityMap(forDeletion));
			forUpdate.addAll(deactivate(forDeletion));
		}
		return forUpdate;
	}

	public CheckoutSessionRequest populateDetailAndRequestCheckout(final String buyerId,
			final Map<Long, ProductResponse> details) {
		final var validItems = populateItems(buyerId, details);
		final var validOrders = validItems.get(false)
			.entrySet()
			.stream()
			.map(entry -> removeInvalid(entry.getKey(), entry.getValue()))
			.collect(Collectors.partitioningBy(order -> !order.getProducts().isEmpty(), Collectors.toSet()));
		final var forUpdate = getAllValid(validOrders.get(true), validItems.get(true).keySet());
		final var forCheckout = forUpdate.stream().map(mapper::toResponse).toList();

		repository.saveAll(addInvalid(forUpdate, validOrders.get(false)));
		itemRepository.saveAll(validItems.get(true).values().stream().flatMap(List::stream).toList());
		return forCheckout.isEmpty() ? null : new CheckoutSessionRequest(forCheckout);
	}

	private Map<Boolean, Map<Order, List<OrderItem>>> populateItems(final String buyerId,
			final Map<Long, ProductResponse> details) {
		final var itemsForCartUpdate = new ArrayList<OrderItem>();

		final var validItems = repository.findAllByBuyerIdAndStatus(buyerId, Status.PROCESSING.toString())
			.stream()
			.map(Order::getProducts)
			.flatMap(List::stream)
			.map(item -> populateAndAdjust(details.get(item.getProductId()), item, itemsForCartUpdate))
			.collect(Collectors.partitioningBy(item -> item.getQuantity() > 0,
					Collectors.groupingBy(OrderItem::getOrder)));

		if (!itemsForCartUpdate.isEmpty()) {
			publishUpdateCartItemsEvent(buyerId, toIdQuantityMap(itemsForCartUpdate.stream()));
		}
		return validItems;
	}

	private OrderItem populateAndAdjust(final ProductResponse detail, final OrderItem item,
			final List<OrderItem> itemsForCartUpdate) {
		if (detail.stock() < item.getQuantity()) {
			item.setQuantity(detail.stock());
			itemsForCartUpdate.add(item);
		}
		item.setProductDetail(mapper.toEntity(detail));
		return item;
	}

	private void publishUpdateCartItemsEvent(final String buyerId, final Map<Long, Integer> products) {
		eventPublisher.publishEvent(new UpdateCartItemsEvent(buyerId, products));
	}

	private Map<Long, Integer> toIdQuantityMap(final List<Order> orders) {
		return toIdQuantityMap(orders.stream().map(Order::getProducts).flatMap(List::stream));
	}

	private Map<Long, Integer> toIdQuantityMap(final Stream<OrderItem> stream) {
		return stream.collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
	}

	private Order removeInvalid(final Order order, final List<OrderItem> items) {
		order.getProducts().removeAll(items);
		return order;
	}

	private Set<Order> getAllValid(final Set<Order> validOrders, final Set<Order> validItemOrders) {
		validOrders.addAll(validItemOrders);
		validOrders.forEach(this::activate);
		return validOrders;
	}

	private Set<Order> addInvalid(final Set<Order> validOrders, final Set<Order> invalidOrders) {
		if (!invalidOrders.isEmpty()) {
			validOrders.addAll(deactivate(invalidOrders));
		}
		return validOrders;
	}

	private void activate(final Order order) {
		updateStatus(order, Status.ACTIVE);
		order.computeTotals();
	}

	private Collection<Order> deactivate(final Collection<Order> orders) {
		return updateStatus(orders, Status.INACTIVE);
	}

	public Map<String, BigDecimal> updateStatusAndGetDetails(final String buyerId, final Status status) {
		final var updated = repository
			.saveAll(updateStatus(repository.findAllByBuyerIdAndStatus(buyerId, Status.ACTIVE.toString()), status));
		return Status.CONFIRMED.equals(status)
				? updated.stream().collect(Collectors.toMap(Order::getShopId, Order::getTotal)) : null;
	}

	private Collection<Order> updateStatus(final Collection<Order> orders, final Status status) {
		orders.forEach(order -> updateStatus(order, status));
		return orders;
	}

	private void updateStatus(final Order order, final Status status) {
		order.setStatus(mapper.valueOf(status));
	}

}