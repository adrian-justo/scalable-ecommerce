package com.apj.ecomm.product.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import com.apj.ecomm.product.constants.AppConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String shopId;

	@Column(nullable = false)
	private String shopName;

	private String description;

	private List<String> images;

	@ColumnDefault("'{general}'")
	private Set<String> categories;

	@ColumnDefault("1")
	private Integer stock;

	@Column(precision = AppConstants.PRICE_PRECISION, scale = AppConstants.PRICE_SCALE)
	@ColumnDefault(AppConstants.PRICE_DEFAULT)
	private BigDecimal price;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@Column(columnDefinition = "boolean default false")
	private boolean active;

	public void order(final Integer quantity) {
		stock -= stock > quantity ? quantity : stock;
	}

	public void restock(final Integer quantity) {
		stock += quantity;
	}

}
