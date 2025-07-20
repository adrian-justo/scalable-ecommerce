package com.apj.ecomm.product.domain;

import java.math.BigDecimal;
import java.time.Instant;
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
	Long id;

	@Column(nullable = false)
	String name;

	@Column(nullable = false)
	String shopName;

	String description;
	Set<String> images;

	@ColumnDefault("'{general}'")
	Set<String> categories;

	@ColumnDefault("1")
	Integer stock;

	@Column(precision = AppConstants.PRICE_PRECISION, scale = AppConstants.PRICE_SCALE)
	@ColumnDefault(AppConstants.PRICE_DEFAULT)
	BigDecimal price;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

}
