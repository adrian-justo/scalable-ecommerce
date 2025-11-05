package com.apj.ecomm.order.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@IdClass(OrderItemId.class)
class OrderItem {

	@Id
	private Long id;

	@Id
	private Long productId;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private OrderItemDetail productDetail;

	@Column(nullable = false)
	private Integer quantity;

	private BigDecimal totalPrice;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@MapsId("id")
	private Order order;

	public void setProductDetail(final OrderItemDetail productDetail) {
		if (productDetail != null) {
			setTotalPrice(BigDecimal.valueOf(quantity).multiply(productDetail.getPrice()));
			productDetail.setItem(this);
		}
		this.productDetail = productDetail;
	}

}
