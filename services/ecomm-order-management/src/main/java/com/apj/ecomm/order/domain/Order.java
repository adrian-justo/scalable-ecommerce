package com.apj.ecomm.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
@DynamicInsert
@DynamicUpdate
class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String buyerId;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private DeliveryInformation deliveryInformation;

	@Column(nullable = false)
	private String shopId;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private ShopInformation shopInformation;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<OrderItem> products;

	private BigDecimal total;

	private Integer totalProducts;

	private Integer totalQuantity;

	private String trackingNumber;

	private String courierCode;

	private String status;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	public void setDeliveryInformation(final DeliveryInformation deliveryInformation) {
		deliveryInformation.setOrder(this);
		this.deliveryInformation = deliveryInformation;
	}

	public void setShopInformation(final ShopInformation shopInformation) {
		shopInformation.setOrder(this);
		this.shopInformation = shopInformation;
	}

	public void setProducts(final List<OrderItem> products) {
		this.products = products;
		initializeProducts();
	}

	public void initializeProducts() {
		if (products != null) {
			products.forEach(p -> p.setOrder(this));
			computeTotals();
		}
	}

	public void computeTotals() {
		setTotal(products.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
		setTotalProducts(products.size());
		setTotalQuantity(products.stream().map(OrderItem::getQuantity).reduce(0, Integer::sum));
	}

}
