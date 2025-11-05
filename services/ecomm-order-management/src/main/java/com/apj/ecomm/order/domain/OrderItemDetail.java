package com.apj.ecomm.order.domain;

import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
class OrderItemDetail {

	private String image;

	private String name;

	private BigDecimal price;

	@Id
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne
	@MapsId
	private OrderItem item;

}